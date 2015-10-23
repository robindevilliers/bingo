package uk.co.malbec.bingo.load;


import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import uk.co.malbec.bingo.present.request.*;
import uk.co.malbec.bingo.present.response.PlayResponse;
import uk.co.malbec.bingo.present.response.PollMessageResponse;
import uk.co.malbec.bingo.present.response.PollMessagesResponse;
import uk.co.malbec.bingo.present.response.PollStateResponse;
import uk.co.malbec.hound.Hound;
import uk.co.malbec.hound.OperationException;
import uk.co.malbec.hound.OperationType;
import uk.co.malbec.hound.Transition;
import uk.co.malbec.hound.reporter.HtmlReporter;
import uk.co.malbec.hound.sampler.HybridSampler;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static javax.ws.rs.client.Entity.entity;
import static org.joda.time.DateTime.now;

public class LoadTestApplication {

    //TODO
    //add configurable parameters (thresholds etc)
    //add configurable replacement for session object

    private static Random randomGenerator = new Random();

    public enum BingoOperationType implements OperationType {
        REGISTER, LOGIN, TOP_UP, ENTER_LOBBY, JOIN_PLAY, POLL_STATE, ANTE_IN, POLL_CHAT_MESSAGES, SEND_CHAT_MESSAGE
    }

    public static class ListPlayResponse extends GenericType<List<PlayResponse>> {
    }

    public static void main(String[] args) throws IOException {

        MongoClient mongoClient = new MongoClient();
        MongoDatabase db = mongoClient.getDatabase("bingo");
        db.getCollection("user").drop();

        File reportsDirectory = new File(format("%s/reports/%s", System.getProperty("user.dir"), System.currentTimeMillis()));

        Hound hound = new Hound()
                .shutdownTime(now().plusMinutes(1));

        hound.configureSampler(HybridSampler.class)
                .setSampleDirectory(new File(reportsDirectory, "data"));

        hound.configureReporter(HtmlReporter.class)
                .setReportsDirectory(reportsDirectory)
                .setDescription("Bingo performance test.")
                .addBulletPoint("1000 users")
                .addBulletPoint("mongo locking")
                .addBulletPoint("memory based chat")
                .addBulletPoint("mongo all collections, fsynced persistence")
                .setExecuteTime(now());

        configureOperations(hound);

        range(0, 1000).forEach(i -> {
            Client client = new ResteasyClientBuilder().connectionPoolSize(2).build();
            WebTarget target = client.target("http://localhost:8080");

            hound
                    .createUser()
                    .addToSession("index", i)
                    .registerSupplier(BingoServer.class, () -> new BingoServer(target))
                    .start("user" + i, new Transition(BingoOperationType.REGISTER, now()));
        });


        hound.waitFor();

        new ProcessBuilder("chromium-browser", reportsDirectory.getAbsolutePath() + "/index.html").start();
    }


    private static void configureOperations(Hound hound) {
        hound
                .register(BingoOperationType.REGISTER, BingoServer.class, (bingo, context) -> {
                    Integer index = (Integer) context.getSession().get("index");

                    bingo.post(
                            "register",
                            new RegisterRequest("user@me.com", "username" + index, "password" + index, "1234567812345678", "Visa", "08/19", "111"),
                            204,
                            "registration request failed"
                    );

                    context.schedule(new Transition(BingoOperationType.ENTER_LOBBY, now()));
                })
                .register(BingoOperationType.LOGIN, BingoServer.class, (bingo, context) -> {

                    bingo.post(
                            "login",
                            new LoginRequest("robin", "lizard")
                            , 204, "login request failed"
                    );

                    context.schedule(new Transition(BingoOperationType.ENTER_LOBBY, now()));
                })
                .register(BingoOperationType.ENTER_LOBBY, BingoServer.class, (bingo, context) -> {

                    List<PlayResponse> plays = bingo.get(
                            "lobby",
                            200,
                            "enter lobby request failed"
                    ).readEntity(new ListPlayResponse());


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

                    bingo.post(
                            "topup",
                            new TopupRequest("50"),
                            204,
                            "topup request failed"
                    );

                    context.schedule(new Transition(BingoOperationType.JOIN_PLAY, now()));
                })
                .register(BingoOperationType.JOIN_PLAY, BingoServer.class, (bingo, context) -> {
                    PlayResponse play = (PlayResponse) context.getSession().get("game");
                    context.trace("joining game " + play.getGame().getTitle());

                    bingo.post(
                            "play",
                            play.getGame().getId().toString(),
                            204,
                            "join game request failed"
                    );

                    context.schedule(new Transition(BingoOperationType.POLL_STATE, now().plusSeconds(1)));
                    context.schedule(new Transition(BingoOperationType.POLL_CHAT_MESSAGES, now().plusSeconds(randomGenerator.nextInt(40))));
                })
                .register(BingoOperationType.POLL_STATE, BingoServer.class, (bingo, context) -> {
                    PlayResponse play = (PlayResponse) context.getSession().get("game");

                    PollStateResponse pollStateResponse = bingo.get(
                            "play",
                            "gameId",
                            play.getGame().getId().toString(),
                            200,
                            "poll request failed"
                    ).readEntity(PollStateResponse.class);

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

                    bingo.post(
                            "play/ante-in",
                            new AnteInRequest(play.getGame().getId(), tickets),
                            200,
                            "ante-in request failed"
                    );
                })
                .register(BingoOperationType.POLL_CHAT_MESSAGES, BingoServer.class, (bingo, context) -> {
                    PlayResponse play = (PlayResponse) context.getSession().get("game");

                    Integer messageIndex = (Integer) context.getSession().get("messageIndex");
                    if (messageIndex == null) {
                        messageIndex = 0;
                        context.getSession().put("messageIndex", 0);
                        context.schedule(new Transition(BingoOperationType.SEND_CHAT_MESSAGE, now().plusSeconds(randomGenerator.nextInt(10) + 10)));
                    }

                    PollMessagesResponse response = bingo.post(
                            "poll-messages",
                            new PollMessagesRequest(messageIndex, play.getGame().getTitle()),
                            200,
                            "poll-messages request failed"
                    ).readEntity(PollMessagesResponse.class);

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

                    bingo.post(
                            "send-message",
                            new SendMessageRequest(chatRoom, username, message),
                            204,
                            "send-message request failed"
                    );

                    context.schedule(new Transition(BingoOperationType.SEND_CHAT_MESSAGE, now().plusSeconds(randomGenerator.nextInt(10) + 10)));
                });
    }

    public static class BingoServer {
        private WebTarget target;

        public BingoServer(WebTarget target) {
            this.target = target;
        }

        public Response post(String path, Object request, int responseCode, String errorMessage) {
            return execute(responseCode, errorMessage, target -> target.path(path).request().post(entity(request, "application/json")));
        }

        public Response get(String path, int responseCode, String errorMessage) {
            return execute(responseCode, errorMessage, target -> target.path(path).request().get());
        }

        public Response get(String path, String name, String value, int responseCode, String errorMessage) {
            return execute(responseCode, errorMessage, target -> target.path(path).queryParam(name, value).request().get());
        }

        private Response execute(int responseCode, String errorMessage, Function<WebTarget, Response> function) {

            try {
                Response response = function.apply(target);
                if (response.getStatus() != responseCode) {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append(format("Status code expected should be %s but was %s\n", responseCode, response.getStatus()));
                    buffer.append(format("HTTP %s\n", response.getStatus()));
                    response.getHeaders().forEach((name, value) -> {
                        buffer.append(format("%s: %s\n", name, value));
                    });
                    buffer.append("\n");
                    String body = response.readEntity(String.class);
                    if (body != null) {
                        buffer.append(body);
                    }

                    throw new OperationException(errorMessage, buffer.toString());
                }
                return response;
            } catch (ProcessingException e) {
                throw new OperationException(errorMessage, e.getLocalizedMessage());
            }
        }
    }
}
