package uk.co.malbec.bingo.process;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.co.malbec.bingo.present.request.RegisterRequest;
import uk.co.malbec.bingo.model.User;
import uk.co.malbec.bingo.persistence.UsersRepository;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class RegisterProcess {

    @Autowired
    private UsersRepository usersRepository;

    @RequestMapping(value = "register", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ResponseEntity register(@Valid @RequestBody() RegisterRequest register, HttpSession httpSession) {

        //TODO
        //email address already taken
        //username already taken
        //payments details invalid


        User user = new User(
                register.getEmailAddress(),
                register.getUsername(),
                register.getPassword(),
                register.getCardNumber(),
                register.getCardType(),
                register.getExpiryDate(),
                register.getSecurityNumber()
        );

        usersRepository.save_ReleaseLock(user);

        httpSession.setAttribute("user", user.getUsername());

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
