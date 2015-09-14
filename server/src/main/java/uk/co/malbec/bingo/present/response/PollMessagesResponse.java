package uk.co.malbec.bingo.present.response;


import java.util.List;

public class PollMessagesResponse {

    private List<PollMessageResponse> messages;

    public PollMessagesResponse(List<PollMessageResponse> messages) {
        this.messages = messages;
    }

    public List<PollMessageResponse> getMessages() {
        return messages;
    }
}
