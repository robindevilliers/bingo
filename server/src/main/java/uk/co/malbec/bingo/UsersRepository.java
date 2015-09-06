package uk.co.malbec.bingo;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UsersRepository {

    private Map<String, User> users = new HashMap<>();

    {
        users.put("robin", new User("robindevilliers@me.com", "robin", "lizard", "1234123412", "Visa", "04/18", "789"));

        for (int i = 0; i < 200; i++) {
            String username = Integer.toString(i);
            users.put(username, new User("robindevilliers@me.com", username, username, "1234123412", "Visa", "04/18", "789"));
        }
    }


    public void add(User user) {
        users.put(user.getUsername(), user);
    }

    public User get(String username) {
        return users.get(username);
    }
}
