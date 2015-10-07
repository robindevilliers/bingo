package uk.co.malbec.bingo.present.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class LoginRequest {

    @NotNull
    @Size(min=4, max=15)
    private String username;

    @NotNull
    @Size(min=4, max=15)
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
