package uk.co.malbec.bingo.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.co.malbec.bingo.present.request.LoginRequest;
import uk.co.malbec.bingo.model.User;
import uk.co.malbec.bingo.persistence.UsersRepository;

import javax.servlet.http.HttpSession;

@Controller
public class LoginProcess {

    @Autowired
    private UsersRepository usersRepository;

    @RequestMapping(value = "login", method = RequestMethod.POST)
    public ResponseEntity login(@RequestBody() LoginRequest login, HttpSession httpSession) {
        User user = usersRepository.get(login.getUsername());
        if (user != null) {
            if (user.getPassword().equals(login.getPassword())) {
                httpSession.setAttribute("user", user);
                return new ResponseEntity(HttpStatus.NO_CONTENT);
            }
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
}
