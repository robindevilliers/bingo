package uk.co.malbec.bingo.present.request;


import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class PollMessagesRequest {

    @NotNull
    @Min(0)
    private int messageIndex;

    @NotNull
    private String chatRoom;

    public PollMessagesRequest() {
    }

    public PollMessagesRequest(int messageIndex, String chatRoom) {
        this.messageIndex = messageIndex;
        this.chatRoom = chatRoom;
    }

    public int getMessageIndex() {
        return messageIndex;
    }

    public String getChatRoom() {
        return chatRoom;
    }
}
