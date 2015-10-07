package uk.co.malbec.bingo.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.co.malbec.bingo.*;
import uk.co.malbec.bingo.model.*;
import uk.co.malbec.bingo.persistence.PlaysRepository;
import uk.co.malbec.bingo.present.request.AnteInRequest;
import uk.co.malbec.bingo.present.response.ErrorCode;
import uk.co.malbec.bingo.present.response.ErrorResponse;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.*;

import static org.joda.time.DateTime.now;


@Controller
@RequestMapping("play")
public class AnteInProcess {

    @Autowired
    private PlaysRepository playsRepository;

    @Autowired
    private GameEngine gameEngine;

    @Autowired
    private PollPlayStateProcess pollPlayStateProcess;

    @RequestMapping(method = RequestMethod.POST, value = "/ante-in", produces = "application/json; charset=utf-8")
    public ResponseEntity anteIn(@Valid @RequestBody AnteInRequest anteIn, HttpSession session) {

        int count = 1;
        for (Integer i : anteIn.getTickets().keySet()){
            if (i != count){
                return new ResponseEntity<>(new ErrorResponse(ErrorCode.CLIENT_INVALID_TICKET_INDEXES, null), HttpStatus.BAD_REQUEST);
            }
            count++;
        }

        User user = (User) session.getAttribute("user");
        Play play = playsRepository.getCurrentPlay(anteIn.getGameId());
        if (play == null) {
            return new ResponseEntity<>(new ErrorResponse(ErrorCode.CLIENT_GAME_NOT_FOUND, null), HttpStatus.BAD_REQUEST);
        }

        if (play.getStartTime().minusSeconds(5).isBefore(now())){
            return new ResponseEntity<>(new ErrorResponse(ErrorCode.CLIENT_GAME_IN_PROGRESS, null), HttpStatus.BAD_REQUEST);
        }


        int numberOfTickets = (int) anteIn.getTickets().values().stream().filter(m -> m.get("selected")).count();
        int ticketFee = play.getGame().getTicketFee();
        if (ticketFee * numberOfTickets > user.getWallet()) {
            return new ResponseEntity<>(new ErrorResponse(ErrorCode.CLIENT_INSUFFICIENT_FUNDS, null), HttpStatus.BAD_REQUEST);
        }
        user.setWallet(user.getWallet() - (ticketFee * numberOfTickets));

        anteIn.getTickets().forEach((ticketNo, selected) -> {
            if (selected.get("selected")) {
                Ticket ticketBean = gameEngine.generateTicket(user.getUsername(), ticketNo);
                while (play.hasTicket(ticketBean.getKey())) {
                    ticketBean = gameEngine.generateTicket(user.getUsername(), ticketNo);
                }
                play.addTicket(ticketBean.getKey(), ticketBean);
            }
        });

        return pollPlayStateProcess.pollPlayState(anteIn.getGameId().toString(), session);
    }

}
