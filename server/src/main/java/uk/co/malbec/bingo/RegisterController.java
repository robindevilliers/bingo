package uk.co.malbec.bingo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("register")
public class RegisterController {

    @Autowired
    private UsersRepository usersRepository;

    @RequestMapping(method= RequestMethod.POST)
    public ResponseEntity register(@RequestBody() Register register) {

        usersRepository.add(
                new User(
                        register.getEmailAddress(),
                        register.getUsername(),
                        register.getPassword(),
                        register.getCardNumber(),
                        register.getCardType(),
                        register.getExpiryDate(),
                        register.getSecurityNumber()
                )
        );

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
