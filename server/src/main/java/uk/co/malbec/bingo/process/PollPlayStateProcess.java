package uk.co.malbec.bingo.process;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.co.malbec.bingo.GameEngine;
import uk.co.malbec.bingo.model.*;
import uk.co.malbec.bingo.persistence.PlaysRepository;

import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class PollPlayStateProcess {

    @Autowired
    private PlaysRepository playsRepository;

    @Autowired
    private GameEngine gameEngine;

    @RequestMapping(value = "play", method = RequestMethod.GET)
    public ResponseEntity pollPlayState(@RequestParam("gameId") String gameId, HttpSession session) {
        Play play = playsRepository.getCurrentPlay(UUID.fromString(gameId));

        User user = (User) session.getAttribute("user");

        //TODO - assign winnings should happen whenever a user is accessed.
        List<Winnings> resolvedWinnings = new ArrayList();

        for (Winnings winnings : user.getWinningsList()) {
            if (winnings.getDateTime().isBeforeNow()) {
                user.setWallet(user.getWallet() + winnings.getAmount());
                resolvedWinnings.add(winnings);
            }
        }
        user.removeWinnings(resolvedWinnings);


        Map<Integer, Ticket> tickets = new HashMap<>();
        for (Ticket ticket : play.getTickets()) {
            if (ticket.getUsername().equals(user.getUsername())) {
                tickets.put(ticket.getIndex(), ticket);
            }
        }

        int yourBet = tickets.size() * play.getGame().getTicketFee();

        PlayView playView = new PlayView(
                user.getUsername(),
                play.getGame(),
                tickets,
                play.getTotalPot(),
                play.getStartTime(),
                play.getEndTime(),
                yourBet,
                user.getWallet(),
                play.getFullHousePrize(),
                play.getTwoLinesPrize(),
                play.getOneLinePrize(),
                play.getFourCornersPrize(),
                play.getGameScript()
        );

        return new ResponseEntity<>(playView, HttpStatus.OK);
    }

}
