package uk.co.malbec.bingo;



import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import uk.co.malbec.bingo.present.request.AnteInRequest;
import uk.co.malbec.bingo.present.request.LoginRequest;
import uk.co.malbec.bingo.model.Play;
import uk.co.malbec.bingo.present.response.PollStateResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class Robot {

    private static Random randomGenerator = new Random();

    public static void main(String[] args) {

        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        for (int i = 0; i < 200; i++) {
            userPlaysAGame(Integer.toString(i));
        }

    }


    public static void userPlaysAGame(String username) {
        System.out.println("---------------------------- " + username);
        Client client = ClientBuilder.newBuilder().build();

        login(client, username);
        Play play = enterLobby(client);
        joinPlay(client, play);
        pollGameState(client, play);
        anteIn(client, play);
    }


    public static void login(Client client, String username) {
        WebTarget target = client.target("http://localhost:8080/login");
        Response response = target.request().post(Entity.entity(new LoginRequest(username, username), "application/json"));

        if (response.getStatus() != 204) {
            throw new RuntimeException("invalid login");
        }
    }

    public static Play enterLobby(Client client) {
        WebTarget target = client.target("http://localhost:8080/lobby");


        Play play = null;
        while (play == null) {

            Response response = target.request().get();

            if (response.getStatus() != 200) {
                throw new RuntimeException("error entering lobby");
            }

            List<Play> plays = response.readEntity(new GenericType<List<Play>>() {  });

            for (Play p : plays) {
                if (p.getStartTime().minusSeconds(35).isAfterNow()) {
                    play = p;
                    break;
                }
            }
            if (play == null) {
                try {
                    System.out.println("sleep");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return play;
    }

    public static void joinPlay(Client client, Play play) {
        WebTarget target = client.target("http://localhost:8080/play");
        Response response = target.request().post(Entity.entity(play.getGame().getId().toString(), "application/json"));

        if (response.getStatus() != 204) {
            throw new RuntimeException("error joining game");
        }

    }

    private static PollStateResponse pollGameState(Client client, Play play) {
        WebTarget target = client.target("http://localhost:8080/play?gameId=" + play.getGame().getId().toString());
        Response response = target.request().get();

        if (response.getStatus() != 200) {
            throw new RuntimeException("error polling game state");
        }

        return response.readEntity(PollStateResponse.class);
    }

    private static void anteIn(Client client, Play play) {

        Map<Integer, Map<String, Boolean>> tickets = new HashMap<Integer, Map<String, Boolean>>() {{
            put(1, new HashMap<String, Boolean>() {{
                put("selected", true);
            }});
            put(2, new HashMap<String, Boolean>() {{
                put("selected", true);
            }});
            put(3, new HashMap<String, Boolean>() {{
                put("selected", true);
            }});
            put(4, new HashMap<String, Boolean>() {{
                put("selected", true);
            }});
            put(5, new HashMap<String, Boolean>() {{
                put("selected", true);
            }});
            put(6, new HashMap<String, Boolean>() {{
                put("selected", true);
            }});
        }};

        WebTarget target = client.target("http://localhost:8080/play/ante-in");
        Response response = target.request().post(Entity.entity(new AnteInRequest(play.getGame().getId(), tickets), "application/json"));


        if (response.getStatus() != 200) {
            throw new RuntimeException("error ante in");
        }

    }
}
