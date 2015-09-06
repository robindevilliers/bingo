package uk.co.malbec.bingo.model;


public class Register {

    private String emailAddress;

    private String username;

    private String password;

    private String cardNumber;

    private String cardType;

    private String expiryDate;

    private String securityNumber;

    public Register(){

    }

    public Register(String emailAddress, String username, String password, String cardNumber, String cardType, String expiryDate, String securityNumber) {
        this.emailAddress = emailAddress;
        this.username = username;
        this.password = password;
        this.cardNumber = cardNumber;
        this.cardType = cardType;
        this.expiryDate = expiryDate;
        this.securityNumber = securityNumber;
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

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardType() {
        return cardType;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getSecurityNumber() {
        return securityNumber;
    }
}
