package uk.co.malbec.bingo.present.request;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class RegisterRequest {

    @NotNull
    @Pattern(regexp="[a-zA-Z0-9.]+@[a-zA-Z0-9.]+")
    private String emailAddress;

    @NotNull
    @Size(min=4, max=15)
    private String username;

    @NotNull
    @Size(min=4, max=15)
    private String password;

    @NotNull
    @Pattern(regexp="^[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]$")
    private String cardNumber;

    @NotNull
    @Pattern(regexp="(Mastercard|Visa)")
    private String cardType;

    @NotNull
    @Pattern(regexp="[0-9][0-9]/[0-9][0-9]")
    private String expiryDate;

    @NotNull
    @Pattern(regexp="[0-9][0-9][0-9]")
    private String securityNumber;

    public RegisterRequest(){

    }

    public RegisterRequest(String emailAddress, String username, String password, String cardNumber, String cardType, String expiryDate, String securityNumber) {
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
