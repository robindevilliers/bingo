package uk.co.malbec.bingo.present.request;


public class PollMessagesRequest {

    private int messageIndex;

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
