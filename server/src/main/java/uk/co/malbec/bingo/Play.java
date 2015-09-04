package uk.co.malbec.bingo;


import org.joda.time.DateTime;

import java.util.*;

public class Play {

    private UUID id;

    private Game game;

    private Map<String, Ticket> tickets = new HashMap<>();

    private DateTime startTime;
    private DateTime endTime;

    private GameScript gameScript;

    public Play() {
    }

    public Play(UUID id, Game game, DateTime startTime){
        this.id = id;
        this.game = game;
        this.startTime = startTime;
    }


    public UUID getId() {
        return id;
    }

    public Game getGame() {
        return game;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public boolean hasTicket(String key){
        return tickets.containsKey(key);
    }

    public void addTicket(String key, Ticket ticket){
        this.tickets.put(key, ticket);
    }


    public void setTickets(List<Ticket> tickets){
        for (Ticket ticket : tickets){
            this.tickets.put(ticket.getKey(), ticket);
        }
    }

    public GameScript getGameScript() {
        return gameScript;
    }

    public void setGameScript(GameScript gameScript) {
        this.gameScript = gameScript;
    }

    public List<Ticket> getTickets() {
        return new ArrayList<>(tickets.values());
    }
}
