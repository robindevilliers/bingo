package uk.co.malbec.bingo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
@RequestMapping("play")
public class PlayController {

    @Autowired
    private PlaysRepository playsRepository;

    @Autowired
    private GameEngine gameEngine;

    @RequestMapping(method= RequestMethod.POST)
    public ResponseEntity joinPlay(@RequestBody String gameId, HttpSession session) {

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method= RequestMethod.GET)
    public ResponseEntity playLoad(@RequestParam("gameId") String gameId, HttpSession session) {
        Play play = playsRepository.getCurrentPlay(UUID.fromString(gameId));

        User user = (User) session.getAttribute("user");

        //TODO - assign winnings should happen whenever a user is accessed.
        List<Winnings> resolvedWinnings = new ArrayList();

        for (Winnings winnings: user.getWinningsList()){
            if (winnings.getDateTime().isBeforeNow()){
                user.setWallet(user.getWallet() + winnings.getAmount());
                resolvedWinnings.add(winnings);
            }
        }
        user.removeWinnings(resolvedWinnings);


        Map<Integer, Ticket> tickets = new HashMap<>();
        for (Ticket ticket : play.getTickets()){
            if (ticket.getUsername().equals(user.getUsername())){
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

    @RequestMapping(method= RequestMethod.POST, value="/ante-in")
    public ResponseEntity anteIn(@RequestBody AnteIn anteIn, HttpSession session){
        User user = (User) session.getAttribute("user");
        Play play = playsRepository.getCurrentPlay(anteIn.getGameId());

        for (Map.Entry<Integer, Map<String ,Boolean>> ticket: anteIn.getTickets().entrySet()){
            if (ticket.getValue().get("selected")){
                int ticketFee = play.getGame().getTicketFee();
                if (user.getWallet() > ticketFee) {

                    user.setWallet(user.getWallet() - ticketFee);

                    Ticket ticketBean = gameEngine.generateTicket(user.getUsername(), ticket.getKey());
                    while (play.hasTicket(ticketBean.getKey())) {
                        ticketBean = gameEngine.generateTicket(user.getUsername(), ticket.getKey());
                    }
                    play.addTicket(ticketBean.getKey(), ticketBean);
                }
            }
        }
        return playLoad(anteIn.getGameId().toString(), session);
    }

}
