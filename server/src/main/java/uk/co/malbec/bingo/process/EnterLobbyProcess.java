package uk.co.malbec.bingo.process;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.co.malbec.bingo.model.Game;
import uk.co.malbec.bingo.persistence.GamesRepository;
import uk.co.malbec.bingo.model.Play;
import uk.co.malbec.bingo.persistence.PlaysRepository;

import java.util.ArrayList;
import java.util.List;

@Controller
public class EnterLobbyProcess {

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private PlaysRepository playsRepository;


    @RequestMapping(value = "lobby", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public ResponseEntity listCurrentGames() {

        List<Play> plays = new ArrayList<>();

        for (Game game : gamesRepository.getGames()) {
            plays.add(playsRepository.getCurrentPlay(game.getId()));
        }

        return new ResponseEntity<>(plays, HttpStatus.OK);
    }

}
