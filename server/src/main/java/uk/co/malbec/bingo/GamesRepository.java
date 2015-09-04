package uk.co.malbec.bingo;


import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class GamesRepository {

    List<Game> games = new ArrayList<>();

    {
       games.add(new Game(UUID.randomUUID(), "Haloween Horrors", 360, 50, 100));
       games.add(new Game(UUID.randomUUID(), "Sunset Boulevard", 360, 50, 100));
       games.add(new Game(UUID.randomUUID(), "Chevy Chopper", 360, 50, 100));
       games.add(new Game(UUID.randomUUID(), "Tropical Promenade", 360, 50, 100));
       games.add(new Game(UUID.randomUUID(), "Sea Breeze", 360, 50, 100));
       games.add(new Game(UUID.randomUUID(), "Purple Haze", 360, 50, 100));
       games.add(new Game(UUID.randomUUID(), "Moonlight Dream", 360, 50, 100));
       games.add(new Game(UUID.randomUUID(), "Annie Honolulu", 360, 50, 100));

    }


    public List<Game> getGames() {
        return games;
    }
}
