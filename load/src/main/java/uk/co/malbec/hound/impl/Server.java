package uk.co.malbec.hound.impl;


import org.joda.time.DateTime;
import uk.co.malbec.hound.OperationException;
import uk.co.malbec.hound.OperationType;
import uk.co.malbec.hound.Sampler;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static org.joda.time.DateTime.now;
import static uk.co.malbec.hound.Utils.pause;

public class Server extends Thread {

    private Queue<Job> queue = new PriorityBlockingQueue<>();

    private ExecutorService executorService;

    private AtomicInteger busyJobs = new AtomicInteger();

    private Map<OperationType, OperationRecord> operations = new HashMap<>();

    private DateTime shutdownTime;

    private Sampler sampler;

    private Runnable terminationCallback;

    public Server(Map<OperationType, OperationRecord> operations, DateTime shutdownTime, Sampler sampler, Runnable terminationCallBack) {
        executorService = newCachedThreadPool();
        this.operations = operations;
        this.shutdownTime = shutdownTime;
        this.sampler = sampler;
        this.terminationCallback = terminationCallBack;
    }

    public Queue<Job> getQueue() {
        return queue;
    }

    public void run() {

        while (!queue.isEmpty() && shutdownTime.isAfter(now())) {
            while (!queue.peek().isReady() && shutdownTime.isAfter(now())) {
                pause(50);
            }

            busyJobs.incrementAndGet();
            Job job = queue.poll();

            OperationRecord operationRecord = operations.get(job.getTransition().getOperationType());

            if (operationRecord == null) {
                throw new RuntimeException("Operation not found for operation type " + job.getTransition().getOperationType());
            }

            Supplier<?> supplier = job.getResourceSuppliers().get(operationRecord.getClazz());
            if (supplier == null) {
                throw new RuntimeException("Supplier not found for resource of type " + operationRecord.getClazz());
            }

            executorService.execute(() -> {
                job.getOperationContext().trace("executing operation " + job.getTransition().getOperationType());
                DateTime startTime = now();
                String errorMessage = null;
                String detailedErrorMessage = null;
                try {
                    operationRecord.getOperation().execute(supplier.get(), job.getOperationContext());
                } catch (OperationException e){
                    errorMessage = e.getMessage();
                    detailedErrorMessage = e.getDetailedMessage();
                } catch (RuntimeException e) {
                    errorMessage = e.getMessage();
                } finally {
                    DateTime endTime = now();
                    busyJobs.decrementAndGet();
                    sampler.addSample(job.getOperationContext().getName(), job.getTransition().getOperationType().name(), startTime, endTime, errorMessage, detailedErrorMessage);
                }
            });

            //if after processing the job, the queue is empty, but the job is still running, we wait.
            while (queue.isEmpty() && busyJobs.get() > 0 && shutdownTime.isAfter(now())) {
                pause(50);
            }
        }

        executorService.shutdown();

        while (busyJobs.get() > 0) {
            pause(50);
        }

        terminationCallback.run();
    }

}
