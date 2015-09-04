package uk.co.malbec.bingo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.UUID;

public class Ticket {

    private UUID id;

    private String username;

    private int index;

    private Integer[] numbers;

    public Ticket() {
    }

    public Ticket(UUID id, String username, int index, Integer[] numbers) {
        this.id = id;
        this.username = username;
        this.index = index;
        this.numbers  = numbers;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public int getIndex() {
        return index;
    }

    public Integer[] getNumbers() {
        return numbers;
    }

    @JsonIgnore
    public String getKey(){
       return Integer.toString(Arrays.hashCode(numbers));
    }
}
