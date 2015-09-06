package uk.co.malbec.bingo.process;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.co.malbec.bingo.model.Register;
import uk.co.malbec.bingo.model.User;
import uk.co.malbec.bingo.persistence.UsersRepository;

import javax.servlet.http.HttpSession;

@Controller
public class RegisterProcess {

    @Autowired
    private UsersRepository usersRepository;

    @RequestMapping(value = "register", method = RequestMethod.POST)
    public ResponseEntity register(@RequestBody() Register register, HttpSession httpSession) {

        User user = new User(
                register.getEmailAddress(),
                register.getUsername(),
                register.getPassword(),
                register.getCardNumber(),
                register.getCardType(),
                register.getExpiryDate(),
                register.getSecurityNumber()
        );

        usersRepository.add(user);

        httpSession.setAttribute("user", user);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
