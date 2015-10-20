package uk.co.malbec.bingo.process;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.co.malbec.bingo.persistence.PlaysRepository;
import uk.co.malbec.bingo.present.response.PlayResponse;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.co.malbec.bingo.Converters.toPlayResponse;

@Controller
@SuppressWarnings({"UnusedDeclaration"})
public class EnterLobbyProcess {

    @Autowired
    private PlaysRepository playsRepository;

    @RequestMapping(value = "lobby", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public ResponseEntity listCurrentGames() {

        List<PlayResponse> plays = playsRepository.getCurrentPlays_NoLock()
                .stream()
                .map(toPlayResponse())
                .collect(toList());

        return new ResponseEntity<>(plays, HttpStatus.OK);
    }
}
