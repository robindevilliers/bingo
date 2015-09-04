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
@RequestMapping("login")
public class AuthenticateController {

    @Autowired
    private UsersRepository usersRepository;

    @RequestMapping(method= RequestMethod.POST)
    public ResponseEntity login(@RequestBody() Login login, HttpSession httpSession){
        User user = usersRepository.get(login.getUsername());
        if (user != null){
            if (user.getPassword().equals(login.getPassword())){
                httpSession.setAttribute("user", user);
                return new ResponseEntity(HttpStatus.NO_CONTENT);
            }
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

}
