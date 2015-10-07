package uk.co.malbec.bingo.present.response;

public enum ErrorCode {

    CLIENT_INVALID_INPUT("An invalid value was supplied."),
    CLIENT_INVALID_CREDENTIALS("Invalid credentials."),
    CLIENT_INVALID_TICKET_INDEXES("Ticket indexes supplied are invalid."),
    CLIENT_NOT_AUTHORISED("The user is not authorised."),
    CLIENT_AUTHENTICATION_FAILURE_LIMIT_EXCEEDED("The user has failed too authenticate too many times."),
    CLIENT_GAME_NOT_FOUND("The game id supplied is not found."),
    CLIENT_GAME_IN_PROGRESS("The game is already in progress."),
    CLIENT_INSUFFICIENT_FUNDS("There are not enough funds to purchase the tickets."),
    SERVER_UNKNOWN_ERROR("The server experienced an unknown error.");

    private String description;

    ErrorCode(String description){
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
