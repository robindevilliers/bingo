package uk.co.malbec.bingo.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.co.malbec.bingo.model.ChatMessage;
import uk.co.malbec.bingo.persistence.ChatRepository;
import uk.co.malbec.bingo.present.request.SendMessageRequest;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class SendMessageProcess {

    @Autowired
    private ChatRepository chatRepository;

    @RequestMapping(value = "send-message", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ResponseEntity sendMessage(@Valid @RequestBody() SendMessageRequest sendMessageRequest, HttpSession httpSession) {

        chatRepository.addMessage(sendMessageRequest.getChatRoom(), new ChatMessage(sendMessageRequest.getUsername(), sendMessageRequest.getMessage()));

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
