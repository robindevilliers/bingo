package uk.co.malbec.bingo.load;


import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.joda.time.DateTime;
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

    private static Random randomGenerator = new Random();

    public enum BingoOperationType implements OperationType {
        REGISTER, LOGIN, TOP_UP, ENTER_LOBBY, JOIN_PLAY, POLL_STATE, ANTE_IN, POLL_CHAT_MESSAGES, SEND_CHAT_MESSAGE
    }

    public static void main(String[] args) throws IOException {

        MongoClient mongoClient = new MongoClient();
        MongoDatabase db = mongoClient.getDatabase("bingo");
        db.getCollection("user").drop();

        File reportsDirectory = new File(format("%s/reports/%s", System.getProperty("user.dir"), System.currentTimeMillis()));

        Hound<BingoUser> hound = new Hound<BingoUser>()
                .shutdownTime(now().plusMinutes(20));

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
                    .createUser(new BingoUser(i))
                    .registerSupplier(BingoServer.class, () -> new BingoServer(target))
                    .start("user" + i, new Transition(BingoOperationType.REGISTER, now()));
        });


        hound.waitFor();

        new ProcessBuilder("google-chrome", reportsDirectory.getAbsolutePath() + "/index.html").start();
    }


    private static void configureOperations(Hound<BingoUser> hound) {
        hound
                .register(BingoOperationType.REGISTER, BingoServer.class, (bingo, context) -> {

                    bingo.post()
                            .path("register")
                            .requestBody(new RegisterRequest("user@me.com", "username" + context.getSession().getIndex(), "password" + context.getSession().getIndex(), "1234567812345678", "Visa", "08/19", "111"))
                            .expectedResponseCode(204)
                            .errorMessageOnFailure("registration request failed")
                            .execute();

                    context.schedule(new Transition(BingoOperationType.ENTER_LOBBY, now()));
                })
                .register(BingoOperationType.LOGIN, BingoServer.class, (bingo, context) -> {

                    bingo.post()
                            .path("login")
                            .requestBody(new LoginRequest("robin", "lizard"))
                            .expectedResponseCode(204)
                            .errorMessageOnFailure("login request failed")
                            .execute();

                    context.schedule(new Transition(BingoOperationType.ENTER_LOBBY, now()));
                })
                .register(BingoOperationType.ENTER_LOBBY, BingoServer.class, (bingo, context) -> {

                    List<PlayResponse> plays = bingo.get()
                            .path("lobby")
                            .expectedResponseCode(200)
                            .errorMessageOnFailure("enter lobby request failed")
                            .execute()
                            .readEntity(new ListPlayResponse());

                    List<PlayResponse> availableGames = plays
                            .stream()
                            .filter(play -> play.getStartTime().isAfter(now().plusSeconds(35)))
                            .collect(toList());

                    if (availableGames.isEmpty()) {
                        context.schedule(new Transition(BingoOperationType.ENTER_LOBBY, now().plusSeconds(5)));
                        return;
                    }

                    context.getSession().setPlay(availableGames.get(randomGenerator.nextInt(availableGames.size())));
                    context.schedule(new Transition(BingoOperationType.TOP_UP, now().plusSeconds(3)));
                })
                .register(BingoOperationType.TOP_UP, BingoServer.class, (bingo, context) -> {

                    bingo.post()
                            .path("topup")
                            .requestBody(new TopupRequest("50"))
                            .expectedResponseCode(204)
                            .errorMessageOnFailure("topup request failed")
                            .execute();

                    context.schedule(new Transition(BingoOperationType.JOIN_PLAY, now()));
                })
                .register(BingoOperationType.JOIN_PLAY, BingoServer.class, (bingo, context) -> {

                    context.trace("joining play " + context.getSession().getPlay().getGame().getTitle());

                    bingo.post()
                            .path("play")
                            .requestBody(context.getSession().getPlay().getGame().getId().toString())
                            .expectedResponseCode(204)
                            .errorMessageOnFailure("join play request failed")
                            .execute();

                    context.schedule(new Transition(BingoOperationType.POLL_STATE, now().plusSeconds(1)));
                    context.schedule(new Transition(BingoOperationType.POLL_CHAT_MESSAGES, now().plusSeconds(randomGenerator.nextInt(40))));
                })
                .register(BingoOperationType.POLL_STATE, BingoServer.class, (bingo, context) -> {

                    PollStateResponse pollStateResponse = bingo.get()
                            .path("play")
                            .queryParam("gameId", context.getSession().getPlay().getGame().getId().toString())
                            .expectedResponseCode(200)
                            .errorMessageOnFailure("poll request failed")
                            .execute()
                            .readEntity(PollStateResponse.class);

                    //test for the start of a new play
                    if (!pollStateResponse.getStartTime().equals(context.getSession().getStartTime())) {
                        context.getSession().setStartTime(pollStateResponse.getStartTime());

                        long wait = randomGenerator.nextLong() % Math.max(pollStateResponse.getStartTime().getMillis() - 30000 - now().getMillis(), 1);
                        context.schedule(new Transition(BingoOperationType.ANTE_IN, now().plusMillis((int) wait)));

                        context.trace("play starts at " + pollStateResponse.getStartTime());
                    }

                    if (pollStateResponse.getEndTime() == null) {
                        context.schedule(new Transition(BingoOperationType.POLL_STATE, now().plusSeconds(1)));
                    } else {
                        context.trace("play start... and it ends at " + pollStateResponse.getEndTime());
                        context.schedule(new Transition(BingoOperationType.POLL_STATE, now().plusMillis((int) Math.max(pollStateResponse.getEndTime().getMillis() - now().getMillis() + 10000, 0))));
                    }
                })
                .register(BingoOperationType.ANTE_IN, BingoServer.class, (bingo, context) -> {

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

                    bingo.post()
                            .path("play/ante-in")
                            .requestBody(new AnteInRequest(context.getSession().getPlay().getGame().getId(), tickets))
                            .expectedResponseCode(200)
                            .errorMessageOnFailure("ante-in request failed")
                            .execute();
                })
                .register(BingoOperationType.POLL_CHAT_MESSAGES, BingoServer.class, (bingo, context) -> {

                    if (context.getSession().getMessageIndex() == null) {
                        context.getSession().setMessageIndex(0);
                        context.schedule(new Transition(BingoOperationType.SEND_CHAT_MESSAGE, now().plusSeconds(randomGenerator.nextInt(10) + 10)));
                    }

                    PollMessagesResponse response = bingo.post()
                            .path("poll-messages")
                            .requestBody(new PollMessagesRequest(context.getSession().getMessageIndex(), context.getSession().getPlay().getGame().getTitle()))
                            .expectedResponseCode(200)
                            .errorMessageOnFailure("poll-messages request failed")
                            .execute()
                            .readEntity(PollMessagesResponse.class);

                    response.getMessages()
                            .stream()
                            .map(PollMessageResponse::getMessageIndex)
                            .max(Comparator.naturalOrder())
                            .ifPresent(i -> context.getSession().setMessageIndex(i));

                    context.schedule(new Transition(BingoOperationType.POLL_CHAT_MESSAGES, now().plusMillis(500)));
                })
                .register(BingoOperationType.SEND_CHAT_MESSAGE, BingoServer.class, (bingo, context) -> {

                    String chatRoom = context.getSession().getPlay().getGame().getTitle();
                    String username = "user" + context.getSession().getIndex();
                    String message = "message " + context.getSession().getMessageIndex();
                    context.trace("message index " + context.getSession().getMessageIndex());

                    bingo.post()
                            .path("send-message")
                            .requestBody(new SendMessageRequest(chatRoom, username, message))
                            .expectedResponseCode(204)
                            .errorMessageOnFailure("send-message request failed")
                            .execute();

                    context.schedule(new Transition(BingoOperationType.SEND_CHAT_MESSAGE, now().plusSeconds(randomGenerator.nextInt(10) + 10)));
                });
    }

    public static class ListPlayResponse extends GenericType<List<PlayResponse>> {
    }

    public static class BingoUser {

        private int index;

        private PlayResponse play;

        private DateTime startTime;

        private Integer messageIndex;

        public BingoUser(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public PlayResponse getPlay() {
            return play;
        }

        public void setPlay(PlayResponse play) {
            this.play = play;
        }

        public DateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(DateTime startTime) {
            this.startTime = startTime;
        }

        public Integer getMessageIndex() {
            return messageIndex;
        }

        public void setMessageIndex(Integer messageIndex) {
            this.messageIndex = messageIndex;
        }
    }

    public static class BingoServer {
        private WebTarget target;

        public BingoServer(WebTarget target) {
            this.target = target;
        }

        public BingoPost post() {
            return new BingoPost(target);
        }

        public BingoGet get() {
            return new BingoGet(target);
        }

        private static Response executeTarget(WebTarget target, int responseCode, String errorMessage, Function<WebTarget, Response> function) {

            try {
                Response response = function.apply(target);
                if (response.getStatus() != responseCode) {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append(format("Status code expected should be %s but was %s\n", responseCode, response.getStatus()));
                    buffer.append(format("HTTP %s\n", response.getStatus()));
                    response.getHeaders().forEach((name, value) -> buffer.append(format("%s: %s\n", name, value)));
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

        private static class BingoPost extends BingoOperation<BingoPost> {

            private Object request;

            public BingoPost(WebTarget target) {
                super(target);
            }

            public BingoPost requestBody(Object request) {
                this.request = request;
                return this;
            }

            public Response execute() {
                return executeTarget(target, responseCode, errorMessage, target -> target.path(path).request().post(entity(request, "application/json")));
            }
        }

        private static class BingoGet extends BingoOperation<BingoGet> {

            private Map<String, String> queryParams = new HashMap<>();

            public BingoGet(WebTarget target) {
                super(target);
            }

            public BingoGet queryParam(String name, String value) {
                queryParams.put(name, value);
                return this;
            }

            public Response execute() {
                return executeTarget(target, responseCode, errorMessage, target -> {
                    target = target.path(path);
                    for (String name: queryParams.keySet()){
                        target = target.queryParam(name, queryParams.get(name));
                    }
                    return target.request().get();
                });
            }
        }

        private static abstract class BingoOperation<T extends BingoOperation> {
            protected WebTarget target;
            protected String path = "";
            protected int responseCode = 200;
            protected String errorMessage = "request failed";
            private T self;

            @SuppressWarnings("unchecked")
            public BingoOperation(WebTarget target) {
                this.target = target;
                this.self = (T) this;
            }

            public T path(String path) {
                this.path = path;
                return self;
            }

            public T expectedResponseCode(int responseCode) {
                this.responseCode = responseCode;
                return self;
            }

            public T errorMessageOnFailure(String errorMessage) {
                this.errorMessage = errorMessage;
                return self;
            }
        }
    }
}
