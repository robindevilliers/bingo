package uk.co.malbec.bingo.present.response;

import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

public class PlayResponse {
    private UUID id;

    private GameResponse game;

    private List<TicketResponse> tickets;

    private DateTime startTime;

    private DateTime endTime;

    private GameScriptResponse gameScript;

    public PlayResponse(UUID id, GameResponse game, List<TicketResponse> tickets, DateTime startTime, DateTime endTime, GameScriptResponse gameScript) {
        this.id = id;
        this.game = game;
        this.tickets = tickets;
        this.startTime = startTime;
        this.endTime = endTime;
        this.gameScript = gameScript;
    }

    public UUID getId() {
        return id;
    }

    public GameResponse getGame() {
        return game;
    }

    public List<TicketResponse> getTickets() {
        return tickets;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public GameScriptResponse getGameScript() {
        return gameScript;
    }
}
