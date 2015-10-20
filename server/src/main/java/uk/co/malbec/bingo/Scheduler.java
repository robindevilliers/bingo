package uk.co.malbec.bingo;


import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.co.malbec.bingo.model.Game;
import uk.co.malbec.bingo.model.Play;
import uk.co.malbec.bingo.persistence.LoginFailureCountRepository;
import uk.co.malbec.bingo.persistence.PlaysRepository;

import java.util.UUID;

@Component
@SuppressWarnings({"UnusedDeclaration"})
public class Scheduler {

    @Autowired
    private PlaysRepository playsRepository;

    @Autowired
    private GameEngine gameEngine;

    @Autowired
    private LoginFailureCountRepository loginFailureCountRepository;

    @Scheduled(fixedRate = 3600000)
    //@Scheduled(fixedRate = 1000)
    public void purgeFailureCount(){
        loginFailureCountRepository.purge();
    }

    @Scheduled(fixedRate = 1000)
    public void run(){

        for (Play play : playsRepository.getCurrentPlays_NoLock()){

            playsRepository.waitForlock(play.getId());
            try {
                //a game just started
                if (play.getStartTime().isBeforeNow() && play.getEndTime() == null) {
                    //if there are no players, skip the game and start a new game.
                    if (play.getTickets().isEmpty()) {
                        play.setEndTime(play.getStartTime());
                    } else {
                        //valid game, so do a draw.
                        gameEngine.draw(play);
                    }
                }
            } finally {
                playsRepository.save_ReleaseLock(play);
            }

        }

        for (Play play : playsRepository.getCurrentPlays_NoLock()){

            playsRepository.waitForlock(play.getId());

            if (play.getEndTime() != null && play.getEndTime().isBeforeNow()) {
                Game game = play.getGame();

                playsRepository.removePlay_NoLock(play);
                DateTime startTime = DateTime.now().plusSeconds(game.getStagingTime());
                playsRepository.addCurrentPlay_NoLock(new Play(UUID.randomUUID(), game, startTime));
                playsRepository.addClosedPlay_NoLock(game.getId(), play);
            } else {
                playsRepository.save_ReleaseLock(play);
            }

        }

    }
}
