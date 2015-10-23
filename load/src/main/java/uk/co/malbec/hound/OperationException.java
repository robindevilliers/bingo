package uk.co.malbec.hound;


public class OperationException extends RuntimeException {

    private String detailedMessage;

    public OperationException(String message, String detailedMessage){
        super(message);
        this.detailedMessage = detailedMessage;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }
}
