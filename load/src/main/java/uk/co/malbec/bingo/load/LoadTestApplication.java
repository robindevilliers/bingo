package uk.co.malbec.bingo.load;


import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import uk.co.malbec.bingo.present.request.*;
import uk.co.malbec.bingo.present.response.PlayResponse;
import uk.co.malbec.bingo.present.response.PollMessageResponse;
import uk.co.malbec.bingo.present.response.PollMessagesResponse;
import uk.co.malbec.bingo.present.response.PollStateResponse;
import uk.co.malbec.hound.Hound;
import uk.co.malbec.hound.OperationType;
import uk.co.malbec.hound.Transition;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import java.util.*;

import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static javax.ws.rs.client.Entity.entity;
import static org.joda.time.DateTime.now;

public class LoadTestApplication {

    private static Random randomGenerator = new Random();

    public enum BingoOperationType implements OperationType {
        REGISTER, LOGIN, TOP_UP, ENTER_LOBBY, JOIN_PLAY, POLL_STATE, ANTE_IN, POLL_CHAT_MESSAGES, SEND_CHAT_MESSAGE
    }

    public static class ListPlayResponse extends GenericType<List<PlayResponse>> {
    }

    public static void main(String[] args) {


        Hound hound = new Hound();
        configureOperations(hound);
        hound.shutdownTime(now().plusMinutes(1));

        {
            Client client = new ResteasyClientBuilder().connectionPoolSize(2).build();
            WebTarget target = client.target("http://localhost:8080");

            hound
                    .createUser()
                    .addTraceLogger((name, message) -> { System.out.println(now() + "  -  " + name + "  -  " + message);})
                    .addToSession("index", 0)
                    .registerSupplier(WebTarget.class, () -> target)
                    .start("user" + 0, new Transition(BingoOperationType.REGISTER, now()));
        }


        range(1, 1001).forEach(i -> {
            Client client = new ResteasyClientBuilder().connectionPoolSize(2).build();
            WebTarget target = client.target("http://localhost:8080");

            hound
                    .createUser()
                    .addToSession("index", i)
                    .registerSupplier(WebTarget.class, () -> target)
                    .start("user" + i, new Transition(BingoOperationType.REGISTER, now()));
        });
    }


    private static void configureOperations(Hound hound){
        hound.register(BingoOperationType.REGISTER, WebTarget.class, (target, context) -> {


            Integer index = (Integer) context.getSession().get("index");
            of(
                    target.path("register").request().post(entity(new RegisterRequest("user@me.com", "user" + index, "password" + index, "12345678", "Visa", "08/19", "111"), "application/json"))
            )
                    .filter(r -> r.getStatus() == 204).orElseThrow(() -> new RuntimeException("invalid registration"));

            context.schedule(new Transition(BingoOperationType.ENTER_LOBBY, now()));
        });


        hound.register(BingoOperationType.LOGIN, WebTarget.class, (target, context) -> {

            of(
                    target.path("login").request().post(entity(new LoginRequest("robin", "lizard"), "application/json"))
            )
                    .filter(r -> r.getStatus() == 204).orElseThrow(() -> new RuntimeException("invalid login"));

            context.schedule(new Transition(BingoOperationType.ENTER_LOBBY, now()));


        });

        hound.register(BingoOperationType.ENTER_LOBBY, WebTarget.class, (target, context) -> {

            List<PlayResponse> plays = of(
                    target.path("lobby").request().get()
            )
                    .filter(r -> r.getStatus() == 200)
                    .map(r -> r.readEntity(new ListPlayResponse()))
                    .orElseThrow(() -> new RuntimeException("invalid response code"));

            List<PlayResponse> availableGames = plays
                    .stream()
                    .filter(play -> play.getStartTime().isAfter(now().plusSeconds(35)))
                    .collect(toList());


            if (availableGames.isEmpty()) {
                context.schedule(new Transition(BingoOperationType.ENTER_LOBBY, now().plusSeconds(5)));
                return;
            }

            context.getSession().put("game", availableGames.get(randomGenerator.nextInt(availableGames.size())));
            context.schedule(new Transition(BingoOperationType.TOP_UP, now().plusSeconds(3)));
        });


        hound.register(BingoOperationType.TOP_UP, WebTarget.class, (target, context) -> {

            of(
                    target.path("topup").request().post(entity(new TopupRequest("50"), "application/json"))
            )
                    .filter(r -> r.getStatus() == 204).orElseThrow(() -> new RuntimeException("invalid topup"));

            context.schedule(new Transition(BingoOperationType.JOIN_PLAY, now()));
        });


        hound.register(BingoOperationType.JOIN_PLAY, WebTarget.class, (target, context) -> {

            PlayResponse play = (PlayResponse) context.getSession().get("game");
            context.trace("joining game " + play.getGame().getTitle());

            of(
                    target.path("play").request().post(entity(play.getGame().getId().toString(), "application/json"))
            )
                    .filter(r -> r.getStatus() == 204)
                    .orElseThrow(() -> new RuntimeException("error joining game"));

            context.schedule(new Transition(BingoOperationType.POLL_STATE, now().plusSeconds(1)));


            context.schedule(new Transition(BingoOperationType.POLL_CHAT_MESSAGES, now().plusSeconds(randomGenerator.nextInt(40))));

        });

        hound.register(BingoOperationType.POLL_STATE, WebTarget.class, (target, context) -> {

            PlayResponse play = (PlayResponse) context.getSession().get("game");

            PollStateResponse pollStateResponse = of(
                    target.path("play").queryParam("gameId", play.getGame().getId().toString()).request().get()
            )
                    .filter(r -> r.getStatus() == 200)
                    .map(r -> r.readEntity(PollStateResponse.class))
                    .orElseThrow(() -> new RuntimeException("error polling game state"));

            //test for the start of a new game
            if (!pollStateResponse.getStartTime().equals(context.getSession().get("startTime"))) {
                context.getSession().put("startTime", pollStateResponse.getStartTime());

                long wait = randomGenerator.nextLong() % Math.max(pollStateResponse.getStartTime().getMillis() - 35000 - now().getMillis(), 0);
                context.schedule(new Transition(BingoOperationType.ANTE_IN, now().plusMillis((int) wait)));

                context.trace("game starts at " + pollStateResponse.getStartTime());
            }


            if (pollStateResponse.getEndTime() == null) {
                context.schedule(new Transition(BingoOperationType.POLL_STATE, now().plusSeconds(1)));
            } else {
                context.trace("game start... and it ends at " + pollStateResponse.getEndTime());
                context.schedule(new Transition(BingoOperationType.POLL_STATE, now().plusMillis((int) Math.max(pollStateResponse.getEndTime().getMillis() - now().getMillis() + 10000, 0))));
            }
        });


        hound.register(BingoOperationType.ANTE_IN, WebTarget.class, (target, context) -> {

            PlayResponse play = (PlayResponse) context.getSession().get("game");

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

            of(
                    target.path("play").path("ante-in").request().post(entity(new AnteInRequest(play.getGame().getId(), tickets), "application/json"))
            )
                    .filter(r -> r.getStatus() == 200)
                    .orElseThrow(() -> new RuntimeException("error ante in"));
        });

        hound.register(BingoOperationType.POLL_CHAT_MESSAGES, WebTarget.class, (target, context) -> {
            PlayResponse play = (PlayResponse) context.getSession().get("game");

            Integer messageIndex = (Integer) context.getSession().get("messageIndex");
            if (messageIndex == null){
                messageIndex = 0;
                context.getSession().put("messageIndex", 0);
                context.schedule(new Transition(BingoOperationType.SEND_CHAT_MESSAGE, now().plusSeconds(randomGenerator.nextInt(10) + 10)));
            }
            PollMessagesResponse response = of(
                    target.path("poll-messages").request().post(entity(new PollMessagesRequest(messageIndex, play.getGame().getTitle()), "application/json"))
            )
                    .filter(r -> r.getStatus() == 200)
                    .map(r -> r.readEntity(PollMessagesResponse.class))
                    .orElseThrow(() -> new RuntimeException("error polling message state"));


            response.getMessages()
                    .stream()
                    .map(PollMessageResponse::getMessageIndex)
                    .max(Comparator.naturalOrder())
                    .ifPresent(i -> context.getSession().put("messageIndex", i));


            context.schedule(new Transition(BingoOperationType.POLL_CHAT_MESSAGES, now().plusMillis(500)));
        });

        hound.register(BingoOperationType.SEND_CHAT_MESSAGE, WebTarget.class, (target, context) ->{

            PlayResponse play = (PlayResponse) context.getSession().get("game");

            String chatRoom = play.getGame().getTitle();
            String username = "user" + context.getSession().get("index");
            String message = "message " + context.getSession().get("messageIndex");
            context.trace("message index " + context.getSession().get("messageIndex"));
            of(
                    target.path("send-message").request().post(entity(new SendMessageRequest(chatRoom, username, message), "application/json"))
            )
                    .filter(r -> r.getStatus() == 204)
                    .orElseThrow(() -> new RuntimeException("error sending message"));


            context.schedule(new Transition(BingoOperationType.SEND_CHAT_MESSAGE, now().plusSeconds(randomGenerator.nextInt(10) + 10)));
        });
    }


}
