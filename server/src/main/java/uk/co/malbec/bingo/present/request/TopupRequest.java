package uk.co.malbec.bingo.present.request;

public class TopupRequest {

    private String amount;

    public TopupRequest(){

    }

    public TopupRequest(String amount) {
        this.amount = amount;
    }

    public String getAmount() {
        return amount;
    }
}
