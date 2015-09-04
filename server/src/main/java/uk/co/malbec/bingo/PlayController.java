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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

        Map<Integer, Ticket> tickets = new HashMap<>();
        for (Ticket ticket : play.getTickets()){
            if (ticket.getUsername().equals(user.getUsername())){
                tickets.put(ticket.getIndex(), ticket);
            }
        }

        int totalTicketCount = play.getTickets().size();
        int totalPot = totalTicketCount * play.getGame().getTicketFee();
        int yourBet = tickets.size() * play.getGame().getTicketFee();

        //TODO - get these ratios right at some point.


        /*
        200 tickets
        FOUR_CORNERS 53
        ONE_LINE 95
        TWO_LINES 18
        FULL_HOUSE 1
        draw count 64

        100 tickets
        FOUR_CORNERS 30
        ONE_LINE 54
        TWO_LINES 13
        FULL_HOUSE 1
        draw count 66
         */
        int fullHousePrize = totalPot / 2;
        int twoLinesPrize = totalPot / 2 / 10;
        int oneLinesPrize = totalPot / 2 / 10 / 20;
        int fourCornersPrize = totalPot  / 2 / 10 / 20 / 20;

        PlayView playView = new PlayView(user.getUsername(), play.getGame(), tickets, totalPot, play.getStartTime(), play.getEndTime(), yourBet, 4385 /*user.getWallet()*/, fullHousePrize, twoLinesPrize, oneLinesPrize, fourCornersPrize, play.getGameScript() );

        return new ResponseEntity<>(playView, HttpStatus.OK);
    }

    @RequestMapping(method= RequestMethod.POST, value="/ante-in")
    public ResponseEntity anteIn(@RequestBody AnteIn anteIn, HttpSession session){
        User user = (User) session.getAttribute("user");
        Play play = playsRepository.getCurrentPlay(anteIn.getGameId());

        for (Map.Entry<Integer, Map<String ,Boolean>> ticket: anteIn.getTickets().entrySet()){
            if (ticket.getValue().get("selected")){
                Ticket ticketBean  = gameEngine.generateTicket(user.getUsername(), ticket.getKey());
                while (play.hasTicket(ticketBean.getKey())) {
                    ticketBean  = gameEngine.generateTicket(user.getUsername(), ticket.getKey());
                }
                play.addTicket(ticketBean.getKey(), ticketBean);
            }
        }
        return playLoad(anteIn.getGameId().toString(), session);
    }

}
