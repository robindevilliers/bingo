package uk.co.malbec.hound.impl;


import uk.co.malbec.hound.Operation;

public class OperationRecord {

    private Operation operation;
    private Class<?> clazz;

    public OperationRecord(Operation operation, Class<?> clazz) {
        this.operation = operation;
        this.clazz = clazz;
    }

    public Operation getOperation() {
        return operation;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}
