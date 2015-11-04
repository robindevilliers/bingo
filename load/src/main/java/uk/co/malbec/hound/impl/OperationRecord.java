package uk.co.malbec.hound.impl;


import uk.co.malbec.hound.Operation;

public class OperationRecord<T> {

    private Operation<T,?> operation;
    private Class<T> clazz;

    public OperationRecord(Operation<T,?> operation, Class<T> clazz) {
        this.operation = operation;
        this.clazz = clazz;
    }

    public Operation<T,?> getOperation() {
        return operation;
    }

    public Class<T> getClazz() {
        return clazz;
    }
}
