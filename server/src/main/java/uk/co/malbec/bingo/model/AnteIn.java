package uk.co.malbec.bingo.model;


import java.util.Map;
import java.util.UUID;

public class AnteIn {

    private UUID gameId;

    private Map<Integer, Map<String, Boolean>> tickets;


    public AnteIn() {
    }

    public AnteIn(UUID gameId, Map<Integer, Map<String, Boolean>> tickets) {
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
