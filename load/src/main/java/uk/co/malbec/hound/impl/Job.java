package uk.co.malbec.hound.impl;

import uk.co.malbec.hound.OperationContext;
import uk.co.malbec.hound.Transition;

import java.util.Map;
import java.util.function.Supplier;

import static org.joda.time.DateTime.now;

public class Job implements Comparable<Job> {

    private Transition transition;

    private OperationContext<?> operationContext;

    private Map<Class<?>, Supplier<?>> resourceSuppliers;

    public Job(Transition transition, OperationContext<?> operationContext, Map<Class<?>, Supplier<?>> resourceSuppliers) {
        this.transition = transition;
        this.operationContext = operationContext;
        this.resourceSuppliers = resourceSuppliers;
    }

    public Transition getTransition() {
        return transition;
    }

    public OperationContext<?> getOperationContext() {
        return operationContext;
    }

    public Map<Class<?>, Supplier<?>> getResourceSuppliers() {
        return resourceSuppliers;
    }

    public boolean isReady() {
        return transition.getExecuteTime().isBefore(now());
    }

    @Override
    public int compareTo(Job job) {
        return -1 * job.getTransition().getExecuteTime().compareTo(this.transition.getExecuteTime());
    }
}
