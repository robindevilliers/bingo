package uk.co.malbec.bingo.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.co.malbec.bingo.persistence.ChatRepository;
import uk.co.malbec.bingo.present.request.PollMessagesRequest;
import uk.co.malbec.bingo.present.response.PollMessageResponse;
import uk.co.malbec.bingo.present.response.PollMessagesResponse;

import javax.servlet.http.HttpSession;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.co.malbec.bingo.Converters.toPollMessageResponse;

@Controller
public class PollForChatMessagesProcess {

    @Autowired
    private ChatRepository chatRepository;

    @RequestMapping(value = "poll-messages", method = RequestMethod.POST)
    public ResponseEntity<PollMessagesResponse> pollMessages(@RequestBody() PollMessagesRequest pollMessagesRequest, HttpSession httpSession) {
        List<PollMessageResponse> pollMessages = chatRepository.getChatMessagesAfterIndex(pollMessagesRequest.getChatRoom(), pollMessagesRequest.getMessageIndex())
                .stream()
                .map(toPollMessageResponse())
                .collect(toList());

        return new ResponseEntity<>(new PollMessagesResponse(pollMessages), HttpStatus.OK);
    }


}
