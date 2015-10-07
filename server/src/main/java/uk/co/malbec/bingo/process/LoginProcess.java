package uk.co.malbec.bingo.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.co.malbec.bingo.persistence.LoginFailureCountRepository;
import uk.co.malbec.bingo.present.request.LoginRequest;
import uk.co.malbec.bingo.model.User;
import uk.co.malbec.bingo.persistence.UsersRepository;
import uk.co.malbec.bingo.present.response.ErrorCode;
import uk.co.malbec.bingo.present.response.ErrorResponse;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class LoginProcess {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private LoginFailureCountRepository loginFailureCountRepository;


    @RequestMapping(value = "login", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ResponseEntity login(@Valid @RequestBody() LoginRequest login, HttpSession httpSession) {

        if (loginFailureCountRepository.countFailures(login.getUsername()) > 3){
            return new ResponseEntity<>(new ErrorResponse(ErrorCode.CLIENT_AUTHENTICATION_FAILURE_LIMIT_EXCEEDED, null), HttpStatus.BAD_REQUEST);
        }

        User user = usersRepository.get(login.getUsername());
        if (user != null && user.getPassword().equals(login.getPassword())) {
            httpSession.setAttribute("user", user);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        loginFailureCountRepository.addFailure(login.getUsername());
        return new ResponseEntity<>(new ErrorResponse(ErrorCode.CLIENT_INVALID_CREDENTIALS, null), HttpStatus.BAD_REQUEST);
    }
}
