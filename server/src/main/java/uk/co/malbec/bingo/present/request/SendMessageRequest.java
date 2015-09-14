package uk.co.malbec.bingo.present.request;

public class SendMessageRequest {

    private String chatRoom;

    private String username;

    private String message;

    public SendMessageRequest() {
    }

    public SendMessageRequest(String chatRoom, String username, String message) {
        this.chatRoom = chatRoom;
        this.username = username;
        this.message = message;
    }

    public String getChatRoom() {
        return chatRoom;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }
}
