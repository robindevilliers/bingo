package uk.co.malbec.bingo.persistence;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import uk.co.malbec.bingo.model.Game;

import java.util.List;
import java.util.UUID;

@Repository
public class GamesRepository {

    @Autowired
    private MongoOperations mongoOperations;

    public void initialise() {
        mongoOperations.remove(new Query(), "game");

        mongoOperations.save(new Game(UUID.randomUUID(), "Haloween Horrors", 360, 50, 100));
        mongoOperations.save(new Game(UUID.randomUUID(), "Sunset Boulevard", 360, 50, 100));
        mongoOperations.save(new Game(UUID.randomUUID(), "Chevy Chopper", 360, 50, 100));
        mongoOperations.save(new Game(UUID.randomUUID(), "Tropical Promenade", 360, 50, 100));
        mongoOperations.save(new Game(UUID.randomUUID(), "Sea Breeze", 360, 50, 100));
        mongoOperations.save(new Game(UUID.randomUUID(), "Purple Haze", 360, 50, 100));
        mongoOperations.save(new Game(UUID.randomUUID(), "Moonlight Dream", 360, 50, 100));
        mongoOperations.save(new Game(UUID.randomUUID(), "Annie Honolulu", 360, 50, 100));

    }


    public List<Game> getGames() {
        return mongoOperations.findAll(Game.class);
    }
}
