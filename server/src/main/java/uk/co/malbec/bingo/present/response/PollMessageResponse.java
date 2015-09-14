package uk.co.malbec.bingo.present.response;


public class PollMessageResponse {

    private int messageIndex;

    private String username;

    private String message;

    public PollMessageResponse(int messageIndex, String username, String message) {
        this.messageIndex = messageIndex;
        this.username = username;
        this.message = message;
    }

    public int getMessageIndex() {
        return messageIndex;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }
}
