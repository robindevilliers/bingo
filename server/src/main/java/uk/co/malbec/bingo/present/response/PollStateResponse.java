package uk.co.malbec.bingo.present.response;


import org.joda.time.DateTime;

import java.util.Map;

public class PollStateResponse {

    private String username;

    private GameResponse game;

    private Map<Integer, TicketResponse> tickets;

    private int totalPot;

    private int yourBet;

    private int yourWallet;

    private int fullHousePrize;

    private int twoLinesPrize;

    private int oneLinePrize;

    private int fourCornersPrize;

    private DateTime startTime;

    private DateTime endTime;

    private GameScriptResponse gameScript;

    public PollStateResponse(
            String username,
            GameResponse game,
            Map<Integer, TicketResponse> tickets,
            int totalPot,
            DateTime startTime,
            DateTime endTime,
            int yourBet,
            int yourWallet,
            int fullHousePrize,
            int twoLinesPrize,
            int oneLinePrize,
            int fourCornersPrize,
            GameScriptResponse gameScript
    ) {
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

    public GameResponse getGame() {
        return game;
    }

    public Map<Integer, TicketResponse> getTickets() {
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

    public GameScriptResponse getGameScript() {
        return gameScript;
    }
}
