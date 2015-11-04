package uk.co.malbec.hound;

import uk.co.malbec.hound.impl.Job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static java.util.Collections.synchronizedMap;
import static org.joda.time.DateTime.now;

public class OperationContext<U> {

    private U session;

    private Map<Class<?>, Supplier<?>> resourceSuppliers;

    private Queue<Job> queue;

    private String name;

    private List<BiConsumer<String, String>> traceLoggers;

    public OperationContext(String name, Queue<Job> queue, Map<Class<?>, Supplier<?>> resourceSuppliers, U session, List<BiConsumer<String, String>> traceLoggers) {
        this.queue = queue;
        this.name = name;
        this.resourceSuppliers = resourceSuppliers;
        this.session = session;
        this.traceLoggers = traceLoggers;
    }

    public String getName() {
        return name;
    }

    public void trace(String message) {
        traceLoggers.forEach(logger -> logger.accept(name, message));
    }

    public U getSession() {
        return session;
    }

    public void schedule(Transition transition) {
        queue.add(new Job(transition, this, resourceSuppliers));
    }
}
