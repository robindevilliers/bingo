package uk.co.malbec.bingo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("topup")
public class TopupController {

    @Autowired
    private UsersRepository usersRepository;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity register(@RequestBody() Topup topup, HttpSession session) {

        User user = (User) session.getAttribute("user");

        //assume some call to external system that transfers money

        user.setWallet(user.getWallet() + Integer.parseInt(topup.getAmount()) * 100);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
