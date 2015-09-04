package uk.co.malbec.bingo;


public class Register {

    private String emailAddress;

    private String username;

    private String password;

    public Register(){

    }

    public Register(String emailAddress, String username, String password) {
        this.emailAddress = emailAddress;
        this.username = username;
        this.password = password;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


}
