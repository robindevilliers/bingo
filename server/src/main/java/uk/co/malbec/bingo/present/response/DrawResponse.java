package uk.co.malbec.bingo.present.response;

import java.util.List;

public class DrawResponse {
    private Integer number;

    private List<PrizeResponse> prizes;

    public DrawResponse() {
    }

    public DrawResponse(Integer number, List<PrizeResponse> prizes) {
        this.number = number;
        this.prizes = prizes;
    }

    public Integer getNumber() {
        return number;
    }

    public List<PrizeResponse> getPrizes() {
        return prizes;
    }
}
