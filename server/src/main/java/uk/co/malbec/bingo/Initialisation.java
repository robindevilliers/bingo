package uk.co.malbec.bingo;


import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import uk.co.malbec.bingo.model.Game;
import uk.co.malbec.bingo.model.Play;
import uk.co.malbec.bingo.persistence.GamesRepository;
import uk.co.malbec.bingo.persistence.PlaysRepository;

import java.util.UUID;

@Service
public class Initialisation implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private PlaysRepository playsRepository;


    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent ) {

        int secondsOffset = 40;
        for (Game game: gamesRepository.getGames()){
            playsRepository.addCurrentPlay(game.getId(), new Play(UUID.randomUUID(), game, DateTime.now().plusSeconds(secondsOffset)));
            secondsOffset = secondsOffset + 60;
        }

    }
}
