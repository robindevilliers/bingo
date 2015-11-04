package uk.co.malbec.hound;

public interface Operation<T, U> {
    public void execute(T resource, OperationContext<U> operationContext);
}
