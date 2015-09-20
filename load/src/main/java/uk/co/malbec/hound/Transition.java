package uk.co.malbec.hound;

import org.joda.time.DateTime;
import uk.co.malbec.bingo.load.LoadTestApplication;

public class Transition {
    private LoadTestApplication.BingoOperationType operationType;
    private DateTime executeTime;

    public Transition(LoadTestApplication.BingoOperationType operationType, DateTime executeTime) {
        this.operationType = operationType;
        this.executeTime = executeTime;
    }

    public LoadTestApplication.BingoOperationType getOperationType() {
        return operationType;
    }

    public DateTime getExecuteTime() {
        return executeTime;
    }
}
