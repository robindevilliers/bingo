package uk.co.malbec.bingo.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import uk.co.malbec.bingo.model.Play;
import uk.co.malbec.bingo.model.User;

import java.util.UUID;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;
import static uk.co.malbec.bingo.Utilities.pause;

@Repository
@SuppressWarnings({"UnusedDeclaration"})
public class UsersRepository {

    @Autowired
    private MongoOperations mongoOperations;

    public void save_ReleaseLock(User user) {
        user.clearLock();
        mongoOperations.save(user);
    }

    public User get_WaitForLock(String username) {
        UUID lockId = UUID.randomUUID();

        User user = mongoOperations.findAndModify(
                query(where("username").is(username).and("lock").is(null)),
                update("lock", lockId),
                options().returnNew(true),
                User.class
        );

        while (user == null){

            pause(50);

            user = mongoOperations.findAndModify(
                    query(where("username").is(username).and("lock").is(null)),
                    update("lock", lockId),
                    options().returnNew(true),
                    User.class
            );
        }
        return user;
    }

    public User get_NoLock(String username) {
        return mongoOperations.findOne(new Query(Criteria.where("username").is(username)), User.class);
    }
}
