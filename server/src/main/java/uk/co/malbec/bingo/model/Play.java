package uk.co.malbec.bingo.model;


import org.joda.time.DateTime;

import java.util.*;

import static java.util.Collections.synchronizedMap;

public class Play {

    private UUID id;

    private Game game;

    private Map<String, Ticket> tickets = synchronizedMap(new HashMap<>());

    private DateTime startTime;
    private DateTime endTime;

    private GameScript gameScript;

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


    public int getTotalPot(){
        int totalTicketCount = getTickets().size();
        int totalPot = totalTicketCount * getGame().getTicketFee();
        return totalPot;
    }

    //TODO - work out proper ratios
    /*
    200 tickets
    FOUR_CORNERS 53
    ONE_LINE 95
    TWO_LINES 18
    FULL_HOUSE 1
    draw count 64

    100 tickets
    FOUR_CORNERS 30
    ONE_LINE 54
    TWO_LINES 13
    FULL_HOUSE 1
    draw count 66
     */

    public int getFullHousePrize(){
        int fullHousePrize = getTotalPot() / 2;
        return fullHousePrize;
    }

    public int getTwoLinesPrize(){
        int twoLinesPrize = getTotalPot() / 2 / 10;
        return twoLinesPrize;
    }

    public int getOneLinePrize(){
        int oneLinesPrize = getTotalPot() / 2 / 10 / 20;
        return oneLinesPrize;
    }

    public int getFourCornersPrize(){
        int fourCornersPrize = getTotalPot()  / 2 / 10 / 20 / 20;
        return fourCornersPrize;
    }

}
