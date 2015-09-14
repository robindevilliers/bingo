package uk.co.malbec.bingo.model;

public class ChatMessage {

    private int messageIndex;

    private String username;

    private String message;

    public ChatMessage(String username, String message) {
        this.username = username;
        this.message = message;
    }

    public void setMessageIndex(int messageIndex) {
        this.messageIndex = messageIndex;
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
