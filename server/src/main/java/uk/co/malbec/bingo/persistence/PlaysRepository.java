package uk.co.malbec.bingo.persistence;


import org.springframework.stereotype.Repository;
import uk.co.malbec.bingo.model.Play;

import java.util.*;

import static java.util.Collections.synchronizedMap;

@Repository
public class PlaysRepository {

    private Map<UUID, Play> currentPlays = synchronizedMap(new HashMap<UUID, Play>());

    private Map<UUID, List<Play>> closedPlays = new HashMap<UUID, List<Play>>();

    public Play getCurrentPlay(UUID id) {
        return currentPlays.get(id);
    }

    public void addCurrentPlay(UUID id, Play play) {
        currentPlays.put(id, play);
    }

    public void addClosedPlay(UUID id, Play play){
        List<Play> list = closedPlays.get(id);
        if (list == null){
            list = new ArrayList<>();
            closedPlays.put(id, list);
        }
        list.add(play);
    }

    public List<Play> getCurrentPlays() {
        return new ArrayList<>(currentPlays.values());
    }
}
