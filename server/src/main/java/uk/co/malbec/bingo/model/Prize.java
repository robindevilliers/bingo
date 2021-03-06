package uk.co.malbec.bingo.model;


import uk.co.malbec.bingo.PrizeType;

public class Prize {

    private String username;

    private PrizeType prizeType;

    public Prize(String username, PrizeType prizeType) {
        this.username = username;
        this.prizeType = prizeType;
    }

    public String getUsername() {
        return username;
    }

    public PrizeType getPrizeType() {
        return prizeType;
    }
}
