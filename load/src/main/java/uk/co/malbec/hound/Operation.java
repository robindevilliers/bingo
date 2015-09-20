package uk.co.malbec.hound;

public interface Operation<T> {
    public void execute(T resource, OperationContext operationContext);
}
