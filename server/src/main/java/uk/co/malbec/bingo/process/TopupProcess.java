package uk.co.malbec.bingo.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.co.malbec.bingo.present.request.TopupRequest;
import uk.co.malbec.bingo.model.User;
import uk.co.malbec.bingo.persistence.UsersRepository;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class TopupProcess {

    @Autowired
    private UsersRepository usersRepository;

    @RequestMapping(value="topup", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ResponseEntity register(@Valid @RequestBody() TopupRequest topup, HttpSession session) {


        //TODO
        //failure to transfer funds


        //assume some call to external system that transfers money

        User user = usersRepository.get_WaitForLock((String) session.getAttribute("user"));
        try {
            user.setWallet(user.getWallet() + Integer.parseInt(topup.getAmount()) * 100);
        } finally {
            usersRepository.save_ReleaseLock(user);
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
