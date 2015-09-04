package uk.co.malbec.bingo;


import org.joda.time.DateTime;

import java.util.Map;

public class PlayView {

    private String username;

    private Game game;

    private Map<Integer, Ticket> tickets;

    private int totalPot;

    private int yourBet;

    private int yourWallet;

    private int fullHousePrize;

    private int twoLinesPrize;

    private int oneLinePrize;

    private int fourCornersPrize;

    private DateTime startTime;
    private DateTime endTime;

    private GameScript gameScript;

    public PlayView() {
    }

    public PlayView(String username, Game game, Map<Integer, Ticket> tickets, int totalPot, DateTime startTime, DateTime endTime, int yourBet, int yourWallet, int fullHousePrize, int twoLinesPrize, int oneLinePrize, int fourCornersPrize, GameScript gameScript) {
        this.username = username;
        this.game = game;
        this.tickets = tickets;
        this.totalPot = totalPot;
        this.startTime = startTime;
        this.endTime = endTime;
        this.yourBet = yourBet;
        this.yourWallet = yourWallet;
        this.fullHousePrize = fullHousePrize;
        this.twoLinesPrize = twoLinesPrize;
        this.oneLinePrize = oneLinePrize;
        this.fourCornersPrize = fourCornersPrize;
        this.gameScript = gameScript;
    }

    public String getUsername() {
        return username;
    }

    public Game getGame() {
        return game;
    }

    public Map<Integer, Ticket> getTickets() {
        return tickets;
    }

    public int getTotalPot() {
        return totalPot;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public int getYourBet() {
        return yourBet;
    }

    public int getYourWallet() {
        return yourWallet;
    }

    public int getFullHousePrize() {
        return fullHousePrize;
    }

    public int getTwoLinesPrize() {
        return twoLinesPrize;
    }

    public int getOneLinePrize() {
        return oneLinePrize;
    }

    public int getFourCornersPrize() {
        return fourCornersPrize;
    }

    public GameScript getGameScript() {
        return gameScript;
    }
}
