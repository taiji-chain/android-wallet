package io.taiji.wallet.data;

import androidx.annotation.NonNull;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TokenDisplay implements Comparable {

    private String name;
    private String shorty;
    private Long balance;
    private int digits;
    private String tokenAddress;
    private Long totalSupply;

    public TokenDisplay(String name, String shorty, long balance, int digits, String tokenAddress, long totalSupply) {
        this.name = name;
        this.shorty = shorty;
        this.balance = balance;
        this.digits = digits;
        this.tokenAddress = tokenAddress;
        this.totalSupply = totalSupply;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShorty() {
        return shorty;
    }

    public void setShorty(String shorty) {
        this.shorty = shorty;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public int getDigits() {
        return digits;
    }

    public void setDigits(int digits) {
        this.digits = digits;
    }

    public String getTokenAddress() {
        return tokenAddress;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    public Long getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(Long totalSupply) {
        this.totalSupply = totalSupply;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return ((TokenDisplay) o).getShorty().compareTo(shorty);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TokenDisplay that = (TokenDisplay) o;

        if (digits != that.digits) return false;
        if (!name.equals(that.name)) return false;
        return shorty.equals(that.shorty);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + shorty.hashCode();
        result = 31 * result + digits;
        return result;
    }
}
