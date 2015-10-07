package uk.co.malbec.bingo.present.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class TopupRequest {

    @NotNull
    @Pattern(regexp="(5|10|20|50)")
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
