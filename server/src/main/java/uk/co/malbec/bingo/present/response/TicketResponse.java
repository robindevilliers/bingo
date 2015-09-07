package uk.co.malbec.bingo.present.response;

import java.util.UUID;

public class TicketResponse {

    private UUID id;

    private String username;

    private int index;

    private Integer[] numbers;

    public TicketResponse(UUID id, String username, int index, Integer[] numbers) {
        this.id = id;
        this.username = username;
        this.index = index;
        this.numbers = numbers;
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
}
