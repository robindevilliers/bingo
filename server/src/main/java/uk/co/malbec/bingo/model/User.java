package uk.co.malbec.bingo.model;


import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {

    private String emailAddress;

    @Id
    private String username;

    private String password;

    private String cardNumber;

    private String cardType;

    private String expiryDate;

    private String securityNumber;

    private int wallet;

    private List<Winnings> winningsList = new ArrayList<>();

    private UUID lock;

    public User(String emailAddress, String username, String password, String cardNumber, String cardType, String expiryDate, String securityNumber) {
        this.emailAddress = emailAddress;
        this.username = username;
        this.password = password;
        this.cardNumber = cardNumber;
        this.cardType = cardType;
        this.expiryDate = expiryDate;
        this.securityNumber = securityNumber;
    }

    public UUID getLock() {
        return lock;
    }

    public void clearLock() {
        this.lock = null;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getWallet() {
        return wallet;
    }

    public void setWallet(int wallet) {
        this.wallet = wallet;
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

    public void addWinnings(Winnings winnings) {
        winningsList.add(winnings);
    }

    public List<Winnings> getWinningsList(){
        return winningsList;
    }

    public void removeWinnings(List<Winnings> resolvedWinnings) {
        winningsList.removeAll(resolvedWinnings);
    }
}
