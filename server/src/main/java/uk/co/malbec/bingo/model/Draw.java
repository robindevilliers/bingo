package uk.co.malbec.bingo.model;


import java.util.ArrayList;
import java.util.List;

public class Draw {

    private Integer number;

    private List<Prize> prizes = new ArrayList<>();

    public Draw(Integer number) {
        this.number = number;
    }

    public void add(Prize prize){
        prizes.add(prize);
    }

    public Integer getNumber() {
        return number;
    }

    public List<Prize> getPrizes() {
        return prizes;
    }
}
