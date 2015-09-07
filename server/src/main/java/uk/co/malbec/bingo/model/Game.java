package uk.co.malbec.bingo.model;


import java.util.UUID;

public class Game {

    private UUID id;

    private String title;

    private int stagingTime;

    private int ticketFee;

    private int playerLimit;

    public Game(UUID id, String title, int stagingTime, int ticketFee, int playerLimit) {
        this.id = id;
        this.title = title;
        this.stagingTime = stagingTime;
        this.ticketFee = ticketFee;
        this.playerLimit = playerLimit;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getStagingTime() {
        return stagingTime;
    }

    public int getTicketFee() {
        return ticketFee;
    }

    public int getPlayerLimit() {
        return playerLimit;
    }
}
