package uk.co.malbec.bingo.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.co.malbec.bingo.*;
import uk.co.malbec.bingo.model.*;
import uk.co.malbec.bingo.persistence.PlaysRepository;
import uk.co.malbec.bingo.present.request.AnteInRequest;

import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
@RequestMapping("play")
public class AnteInProcess {

    @Autowired
    private PlaysRepository playsRepository;

    @Autowired
    private GameEngine gameEngine;

    @Autowired
    private PollPlayStateProcess pollPlayStateProcess;

    @RequestMapping(method = RequestMethod.POST, value = "/ante-in")
    public ResponseEntity anteIn(@RequestBody AnteInRequest anteIn, HttpSession session) {
        User user = (User) session.getAttribute("user");
        Play play = playsRepository.getCurrentPlay(anteIn.getGameId());

        for (Map.Entry<Integer, Map<String, Boolean>> ticket : anteIn.getTickets().entrySet()) {
            if (ticket.getValue().get("selected")) {
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
        return pollPlayStateProcess.pollPlayState(anteIn.getGameId().toString(), session);
    }

}
