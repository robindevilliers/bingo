package uk.co.malbec.bingo.present.request;


import java.util.Map;
import java.util.UUID;

public class AnteInRequest {

    private UUID gameId;

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
