package uk.co.malbec.bingo.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.co.malbec.bingo.model.Topup;
import uk.co.malbec.bingo.model.User;
import uk.co.malbec.bingo.persistence.UsersRepository;

import javax.servlet.http.HttpSession;

@Controller
public class TopupProcess {

    @Autowired
    private UsersRepository usersRepository;

    @RequestMapping(value="topup", method = RequestMethod.POST)
    public ResponseEntity register(@RequestBody() Topup topup, HttpSession session) {

        User user = (User) session.getAttribute("user");

        //assume some call to external system that transfers money

        user.setWallet(user.getWallet() + Integer.parseInt(topup.getAmount()) * 100);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
