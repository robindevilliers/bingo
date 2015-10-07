package uk.co.malbec.bingo.present.response;


public class ErrorResponse {

    private ErrorCode  errorCode;

    private Object details;

    public ErrorResponse(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorResponse(ErrorCode errorCode, Object details) {
        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object getDetails() {
        return details;
    }
}
