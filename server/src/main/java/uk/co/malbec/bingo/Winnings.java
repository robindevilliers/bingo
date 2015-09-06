package uk.co.malbec.bingo;


import org.joda.time.DateTime;

public class Winnings {

    private int amount;

    private DateTime dateTime;

    public Winnings(int amount, DateTime dateTime) {
        this.amount = amount;
        this.dateTime = dateTime;
    }

    public int getAmount() {
        return amount;
    }

    public DateTime getDateTime() {
        return dateTime;
    }
}
