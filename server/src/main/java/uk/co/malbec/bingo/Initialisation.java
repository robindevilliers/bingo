package uk.co.malbec.bingo;


import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import uk.co.malbec.bingo.model.Game;
import uk.co.malbec.bingo.model.Play;
import uk.co.malbec.bingo.model.User;
import uk.co.malbec.bingo.persistence.GamesRepository;
import uk.co.malbec.bingo.persistence.PlaysRepository;
import uk.co.malbec.bingo.persistence.UsersRepository;

import java.util.UUID;

@Service
public class Initialisation implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private PlaysRepository playsRepository;

    @Autowired
    private UsersRepository usersRepository;


    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent ) {

        gamesRepository.initialise();
        {
            usersRepository.save_ReleaseLock(new User("robindevilliers@me.com", "robin", "lizard", "1234123412341234", "Visa", "04/18", "789"));
        }

        playsRepository.removeAllPlays_NoLock();

        int secondsOffset = 40;
        for (Game game: gamesRepository.getGames()){
            DateTime startTime = DateTime.now().plusSeconds(secondsOffset);
            playsRepository.addCurrentPlay_NoLock(new Play(UUID.randomUUID(), game, startTime));
            secondsOffset = secondsOffset + 60;
        }

    }
}

