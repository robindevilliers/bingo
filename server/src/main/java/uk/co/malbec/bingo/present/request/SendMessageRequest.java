package uk.co.malbec.bingo.present.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SendMessageRequest {

    @NotNull
    private String chatRoom;

    @NotNull
    @Size(min=4, max=15)
    private String username;

    @NotNull
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
