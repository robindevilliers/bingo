package uk.co.malbec.bingo.process;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.co.malbec.bingo.GameEngine;
import uk.co.malbec.bingo.model.Play;
import uk.co.malbec.bingo.model.Ticket;
import uk.co.malbec.bingo.model.User;
import uk.co.malbec.bingo.model.Winnings;
import uk.co.malbec.bingo.persistence.PlaysRepository;
import uk.co.malbec.bingo.persistence.UsersRepository;
import uk.co.malbec.bingo.present.response.PollStateResponse;
import uk.co.malbec.bingo.present.response.TicketResponse;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.co.malbec.bingo.Converters.*;


@Controller
public class PollPlayStateProcess {

    @Autowired
    private PlaysRepository playsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private GameEngine gameEngine;

    @RequestMapping(value = "play", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public ResponseEntity pollPlayState(@RequestParam("gameId") String gameId, HttpSession session) {

        //TODO
        //invalid game id


        User user = usersRepository.get_WaitForLock((String) session.getAttribute("user"));
        try {
            //TODO - assign winnings should happen whenever a user is accessed.
            List<Winnings> winningsToResolve = user.getWinningsList()
                    .stream()
                    .filter(winnings -> winnings.getDateTime().isBeforeNow())
                    .collect(toList());
            winningsToResolve.forEach(winnings -> user.setWallet(user.getWallet() + winnings.getAmount()));
            user.removeWinnings(winningsToResolve);
        } finally {
            usersRepository.save_ReleaseLock(user);
        }


        Play play = playsRepository.getCurrentPlay_WaitForLock(UUID.fromString(gameId));
        try {
            List<Ticket> playerTickets = play.getTickets()
                    .stream()
                    .filter(ticket -> ticket.getUsername().equals(user.getUsername()))
                    .collect(toList());

            PollStateResponse pollState = new PollStateResponse(
                    user.getUsername(),
                    of(play).map(Play::getGame).map(toGameResponse()).get(),
                    playerTickets.stream().map(toTicketResponse()).collect(toMap(TicketResponse::getIndex, identity())),
                    play.getTotalPot(),
                    play.getStartTime(),
                    play.getEndTime(),
                    playerTickets.size() * play.getGame().getTicketFee(),
                    user.getWallet(), //not a lock issue as the lock is released the moment the wallet amount is on the write to the browser anyway.
                    play.getFullHousePrize(),
                    play.getTwoLinesPrize(),
                    play.getOneLinePrize(),
                    play.getFourCornersPrize(),
                    ofNullable(play.getGameScript()).map(toGameScriptView()).orElse(null)
            );

            return new ResponseEntity<>(pollState, HttpStatus.OK);
        } finally {
            playsRepository.save_ReleaseLock(play);
        }
    }

}
