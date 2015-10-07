package uk.co.malbec.bingo.process;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;

@Controller
public class JoinPlayProcess {

    @RequestMapping(value = "play", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ResponseEntity joinPlay(@RequestBody String gameId, HttpSession session) {

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
