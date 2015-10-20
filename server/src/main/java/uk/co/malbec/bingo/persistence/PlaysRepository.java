package uk.co.malbec.bingo.persistence;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import uk.co.malbec.bingo.model.Play;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;
import static uk.co.malbec.bingo.Utilities.pause;

@Repository
public class PlaysRepository {

    @Autowired
    private MongoOperations mongoOperations;

    public void waitForlock(UUID id){
        UUID lockId = UUID.randomUUID();

        Play play = mongoOperations.findAndModify(
                query(where("id").is(id).and("lock").is(null)),
                update("lock", lockId),
                options().returnNew(true),
                Play.class
        );

        while (play == null){

            pause(50);

            play = mongoOperations.findAndModify(
                    query(where("id").is(id).and("lock").is(null)),
                    update("lock", lockId),
                    options().returnNew(true),
                    Play.class
            );
        }
    }

    public Play getCurrentPlay_WaitForLock(UUID id) {
        UUID lockId = UUID.randomUUID();

        Play play = mongoOperations.findAndModify(
                query(where("game.id").is(id)),
                update("lock", lockId),
                options().returnNew(true),
                Play.class
        );

        //timeout after a minute.
        int c = 0;
        while (play == null && c < 1200){

            pause(50);

            play = mongoOperations.findAndModify(
                    query(where("game.id").is(id)),
                    update("lock", lockId),
                    options().returnNew(true),
                    Play.class
            );
            c++;
        }

        return play;
    }

    public void addCurrentPlay_NoLock(Play play) {
        mongoOperations.save(play);
    }

    public void addClosedPlay_NoLock(UUID id, Play play){
        play.clearLock();
        mongoOperations.save(play, "closed");
    }

    public List<Play> getCurrentPlays_NoLock() {
        return mongoOperations.findAll(Play.class);
    }

    public void save_ReleaseLock(Play play) {
        play.clearLock();
        mongoOperations.save(play);
    }

    public void removePlay_NoLock(Play play) {
        mongoOperations.remove(play);
    }

    public void removeAllPlays_NoLock() {
        mongoOperations.remove(new Query(), "play");
    }
}
