package uk.co.malbec.bingo;


import java.util.ArrayList;
import java.util.List;

public class Draw {

    private Integer number;

    private List<Prize> prizes = new ArrayList<>();

    public Draw() {
    }

    public Draw(Integer number) {
        this.number = number;
        this.prizes = prizes;
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
