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
import uk.co.malbec.hound.reporter.HtmlReporter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static javax.ws.rs.client.Entity.entity;
import static org.joda.time.DateTime.now;
import static uk.co.malbec.hound.Utils.pause;

public class LoadTestApplication {

    private static Random randomGenerator = new Random();

    public enum BingoOperationType implements OperationType {
        REGISTER, LOGIN, TOP_UP, ENTER_LOBBY, JOIN_PLAY, POLL_STATE, ANTE_IN, POLL_CHAT_MESSAGES, SEND_CHAT_MESSAGE
    }

    public static class ListPlayResponse extends GenericType<List<PlayResponse>> {
    }

    public static void main(String[] args) {

        Hound hound = new Hound()
                .shutdownTime(now().plusMinutes(1));

        hound.configureReporter(HtmlReporter.class)
                .setReportsDirectory(new File(format("%s/reports/%s", System.getProperty("user.dir"), System.currentTimeMillis())))
                .setDescription("Bingo performance test running 1000 users for 1 minute.")
                .setExecuteTime(now());

        configureOperations(hound);

        range(1, 1000).forEach(i -> {
            Client client = new ResteasyClientBuilder().connectionPoolSize(2).build();
            WebTarget target = client.target("http://localhost:8080");

            hound
                    .createUser()
                    .addToSession("index", i)
                    .registerSupplier(BingoServer.class, () -> new BingoServer(target))
                    .start("user" + i, new Transition(BingoOperationType.REGISTER, now()));
        });


        hound.waitFor();
    }


    private static void configureOperations(Hound hound) {
        hound
                .register(BingoOperationType.REGISTER, BingoServer.class, (bingo, context) -> {
                    Integer index = (Integer) context.getSession().get("index");

                    of(bingo.post("register", new RegisterRequest("user@me.com", "user" + index, "password" + index, "12345678", "Visa", "08/19", "111")))
                            .filter(isStatus(204)).orElseThrow(error("invalid registration"));

                    context.schedule(new Transition(BingoOperationType.ENTER_LOBBY, now()));
                })
                .register(BingoOperationType.LOGIN, BingoServer.class, (bingo, context) -> {

                    of(bingo.post("login", new LoginRequest("robin", "lizard")))
                            .filter(isStatus(204)).orElseThrow(error("invalid login"));

                    context.schedule(new Transition(BingoOperationType.ENTER_LOBBY, now()));
                })
                .register(BingoOperationType.ENTER_LOBBY, BingoServer.class, (bingo, context) -> {

                    List<PlayResponse> plays = of(bingo.get("lobby"))
                            .filter(isStatus(200)).map(r -> r.readEntity(new ListPlayResponse())).orElseThrow(error("invalid response code"));

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
                })
                .register(BingoOperationType.TOP_UP, BingoServer.class, (bingo, context) -> {

                    of(bingo.post("topup", new TopupRequest("50")))
                            .filter(isStatus(204)).orElseThrow(error("invalid topup"));

                    context.schedule(new Transition(BingoOperationType.JOIN_PLAY, now()));
                })
                .register(BingoOperationType.JOIN_PLAY, BingoServer.class, (bingo, context) -> {
                    PlayResponse play = (PlayResponse) context.getSession().get("game");
                    context.trace("joining game " + play.getGame().getTitle());

                    of(bingo.post("play", play.getGame().getId().toString()))
                            .filter(isStatus(204)).orElseThrow(error("error joining game"));

                    context.schedule(new Transition(BingoOperationType.POLL_STATE, now().plusSeconds(1)));
                    context.schedule(new Transition(BingoOperationType.POLL_CHAT_MESSAGES, now().plusSeconds(randomGenerator.nextInt(40))));
                })
                .register(BingoOperationType.POLL_STATE, BingoServer.class, (bingo, context) -> {
                    PlayResponse play = (PlayResponse) context.getSession().get("game");

                    PollStateResponse pollStateResponse = of(bingo.get("play", "gameId", play.getGame().getId().toString()))
                            .filter(isStatus(200)).map(r -> r.readEntity(PollStateResponse.class)).orElseThrow(error("error polling game state"));

                    //test for the start of a new game
                    if (!pollStateResponse.getStartTime().equals(context.getSession().get("startTime"))) {
                        context.getSession().put("startTime", pollStateResponse.getStartTime());

                        long wait = randomGenerator.nextLong() % Math.max(pollStateResponse.getStartTime().getMillis() - 30000 - now().getMillis(), 1);
                        context.schedule(new Transition(BingoOperationType.ANTE_IN, now().plusMillis((int) wait)));

                        context.trace("game starts at " + pollStateResponse.getStartTime());
                    }

                    if (pollStateResponse.getEndTime() == null) {
                        context.schedule(new Transition(BingoOperationType.POLL_STATE, now().plusSeconds(1)));
                    } else {
                        context.trace("game start... and it ends at " + pollStateResponse.getEndTime());
                        context.schedule(new Transition(BingoOperationType.POLL_STATE, now().plusMillis((int) Math.max(pollStateResponse.getEndTime().getMillis() - now().getMillis() + 10000, 0))));
                    }
                })
                .register(BingoOperationType.ANTE_IN, BingoServer.class, (bingo, context) -> {
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

                    of(bingo.post("play/ante-in", new AnteInRequest(play.getGame().getId(), tickets)))
                            .filter(isStatus(200)).orElseThrow(error("error ante in"));
                })
                .register(BingoOperationType.POLL_CHAT_MESSAGES, BingoServer.class, (bingo, context) -> {
                    PlayResponse play = (PlayResponse) context.getSession().get("game");

                    Integer messageIndex = (Integer) context.getSession().get("messageIndex");
                    if (messageIndex == null) {
                        messageIndex = 0;
                        context.getSession().put("messageIndex", 0);
                        context.schedule(new Transition(BingoOperationType.SEND_CHAT_MESSAGE, now().plusSeconds(randomGenerator.nextInt(10) + 10)));
                    }

                    PollMessagesResponse response = of(bingo.post("poll-messages", new PollMessagesRequest(messageIndex, play.getGame().getTitle())))
                            .filter(isStatus(200)).map(r -> r.readEntity(PollMessagesResponse.class)).orElseThrow(error("error polling message state"));

                    response.getMessages()
                            .stream()
                            .map(PollMessageResponse::getMessageIndex)
                            .max(Comparator.naturalOrder())
                            .ifPresent(i -> context.getSession().put("messageIndex", i));

                    context.schedule(new Transition(BingoOperationType.POLL_CHAT_MESSAGES, now().plusMillis(500)));
                })
                .register(BingoOperationType.SEND_CHAT_MESSAGE, BingoServer.class, (bingo, context) -> {
                    PlayResponse play = (PlayResponse) context.getSession().get("game");

                    String chatRoom = play.getGame().getTitle();
                    String username = "user" + context.getSession().get("index");
                    String message = "message " + context.getSession().get("messageIndex");
                    context.trace("message index " + context.getSession().get("messageIndex"));

                    of(bingo.post("send-message", new SendMessageRequest(chatRoom, username, message)))
                            .filter(isStatus(204)).orElseThrow(error("error sending message"));

                    context.schedule(new Transition(BingoOperationType.SEND_CHAT_MESSAGE, now().plusSeconds(randomGenerator.nextInt(10) + 10)));
                });
    }

    public static Supplier<? extends RuntimeException> error(String message) {
        return () -> new RuntimeException(message);
    }

    public static Predicate<? super Response> isStatus(int code) {
        return r -> r.getStatus() == code;
    }

    public static class BingoServer {
        private WebTarget target;

        public BingoServer(WebTarget target) {
            this.target = target;
        }

        public Response post(String path, Object request) {
            return target.path(path).request().post(entity(request, "application/json"));
        }

        public Response get(String path) {
            return target.path(path).request().get();
        }

        public Response get(String path, String name, String value) {
            return target.path(path).queryParam(name, value).request().get();
        }
    }
}
