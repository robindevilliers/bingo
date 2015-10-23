package uk.co.malbec.hound;


public class Sample {

    private boolean ok;

    private String username;

    private String operationName;

    private long start;

    private long end;

    private String errorMessage;

    private String detailedErrorMessage;

    public Sample(boolean ok, String username, String operationName, long start, long end, String errorMessage, String detailedErrorMessage) {
        this.ok = ok;
        this.username = username;
        this.operationName = operationName;
        this.start = start;
        this.end = end;
        this.errorMessage = errorMessage;
        this.detailedErrorMessage = detailedErrorMessage;
    }

    public boolean isOk() {
        return ok;
    }

    public String getUsername() {
        return username;
    }

    public String getOperationName() {
        return operationName;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getDetailedErrorMessage() {
        return detailedErrorMessage;
    }
}
