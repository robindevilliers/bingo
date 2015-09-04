package uk.co.malbec.bingo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("lobby")
public class LobbyController {

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private PlaysRepository  playsRepository;


    @RequestMapping(method= RequestMethod.GET, produces = "application/json; charset=utf-8")
    public ResponseEntity listCurrentGames() {

        List<Play> plays = new ArrayList<>();

        for (Game game : gamesRepository.getGames()){
            plays.add(playsRepository.getCurrentPlay(game.getId()));
        }

        return new ResponseEntity<>(plays, HttpStatus.OK);
    }

}
