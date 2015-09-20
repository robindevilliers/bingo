package uk.co.malbec.bingo.present.response;

import uk.co.malbec.bingo.PrizeType;

public class PrizeResponse {

    private String username;

    private PrizeType prizeType;

    public PrizeResponse() {
    }

    public PrizeResponse(String username, PrizeType prizeType) {
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
