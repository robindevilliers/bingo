package uk.co.malbec.hound.impl;


import org.joda.time.DateTime;
import uk.co.malbec.hound.OperationType;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.joda.time.DateTime.now;

public class Server extends Thread {

    private Queue<Job> queue = new PriorityBlockingQueue<>();

    private ExecutorService executorService = Executors.newFixedThreadPool(50);

    private AtomicInteger busyJobs = new AtomicInteger();

    private Map<OperationType, OperationRecord> operations = new HashMap<>();

    private DateTime shutdownTime;

    private Sampler sampler;

    public Server(Map<OperationType, OperationRecord> operations, DateTime shutdownTime, Sampler sampler) {
        this.operations = operations;
        this.shutdownTime = shutdownTime;
        this.sampler = sampler;
    }

    public Queue<Job> getQueue() {
        return queue;
    }

    public void run() {

        while (!queue.isEmpty() && shutdownTime.isAfter(now())) {
            try {

                while (!queue.peek().isReady() && shutdownTime.isAfter(now())) {
                    sleep(50);
                }

                busyJobs.incrementAndGet();
                Job job = queue.poll();

                OperationRecord operationRecord = operations.get(job.getTransition().getOperationType());

                if (operationRecord == null) {
                    throw new RuntimeException("Operation not found for operation type " + job.getTransition().getOperationType());
                }

                Supplier<?> supplier = job.getResourceSuppliers().get(operationRecord.getClazz());
                if (supplier == null){
                    throw new RuntimeException("Supplier not found for resource of type " + operationRecord.getClazz());
                }

                executorService.execute(() -> {
                    job.getOperationContext().trace("executing operation " + job.getTransition().getOperationType());
                    DateTime startTime = now();
                    try {
                        operationRecord.getOperation().execute(supplier.get(), job.getOperationContext());
                    } finally {
                        DateTime endTime = now();
                        busyJobs.decrementAndGet();
                        sampler.addSample(job.getTransition().getOperationType().name(), Thread.currentThread().getId(), startTime, endTime);
                    }
                });

                //if after processing the job, the queue is empty, but the job is still running, we wait.
                while (queue.isEmpty() && busyJobs.get() > 0 && shutdownTime.isAfter(now())) {
                    sleep(50);
                }

            } catch (InterruptedException e) {
            }
        }

        executorService.shutdown();

        while (busyJobs.get() > 0) {
            try {
                sleep(50);
            } catch (InterruptedException e) {
            }
        }


        sampler.generateReport();
    }

}
