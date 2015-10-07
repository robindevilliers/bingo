package uk.co.malbec.bingo.present.request;


import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public class AnteInRequest {

    @NotNull
    private UUID gameId;

    @NotNull
    private Map<Integer, Map<String, Boolean>> tickets;

    public AnteInRequest() {
    }

    public AnteInRequest(UUID gameId, Map<Integer, Map<String, Boolean>> tickets) {
        this.gameId = gameId;
        this.tickets = tickets;
    }

    public UUID getGameId() {
        return gameId;
    }

    public Map<Integer, Map<String, Boolean>> getTickets() {
        return tickets;
    }
}
