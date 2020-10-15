package io.taiji.wallet.data;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TransactionDisplay implements Comparable {
    private String walletName;
    private String fromAddress;
    private String toAddress;
    private long amount;
    private boolean isApp;
    private String type;
    private String id;
    private long timestamp;

    public TransactionDisplay(String fromAddress, String toAddress, long amount, boolean isApp, String type, String id, long timestamp, String walletName) {
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.amount = amount;
        this.isApp = isApp;
        this.type = type;
        this.id = id;
        this.timestamp = timestamp;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public boolean isApp() {
        return isApp;
    }

    public void setApp(boolean app) {
        isApp = app;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    @Override
    public int compareTo(Object o) {
        if (this.getTimestamp() < ((TransactionDisplay) o).getTimestamp())
            return 1;
        if (this.getTimestamp() == ((TransactionDisplay) o).getTimestamp())
            return 0;
        return -1;
    }
}
