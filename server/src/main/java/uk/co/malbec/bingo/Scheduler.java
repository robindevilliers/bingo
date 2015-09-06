package uk.co.malbec.bingo;


import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class Scheduler {

    @Autowired
    private PlaysRepository playsRepository;

    @Autowired
    private GameEngine gameEngine;

    @Scheduled(fixedRate = 1000)
    public void run(){

        for (Play play : playsRepository.getCurrentPlays()){

            //a game just started
            if (play.getStartTime().isBeforeNow() && play.getEndTime() == null){

                //if there are no players, skip the game and start a new game.
                if (play.getTickets().isEmpty()){
                    play.setEndTime(play.getStartTime());
                } else {
                    //valid game, so do a draw.
                    gameEngine.draw(play);
                }

            }
        }

        for (Play play : playsRepository.getCurrentPlays()){
            if (play.getEndTime() != null && play.getEndTime().isBeforeNow()){
                Game game = play.getGame();

                playsRepository.addCurrentPlay(game.getId(), new Play(UUID.randomUUID(), game, DateTime.now().plusSeconds(game.getStagingTime())));
                playsRepository.addClosedPlay(game.getId(), play);
            }
        }

    }
}
