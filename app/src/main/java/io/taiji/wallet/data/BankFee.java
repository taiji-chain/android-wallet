package io.taiji.wallet.data;

public class BankFee {
    int interChain;
    int innerChain;
    int application;
    String bankAddress;

    public BankFee(int interChain, int innerChain, int application, String bankAddress) {
        this.interChain = interChain;
        this.innerChain = innerChain;
        this.application = application;
        this.bankAddress = bankAddress;
    }

    public int getInterChain() {
        return interChain;
    }

    public void setInterChain(int interChain) {
        this.interChain = interChain;
    }

    public int getInnerChain() {
        return innerChain;
    }

    public void setInnerChain(int innerChain) {
        this.innerChain = innerChain;
    }

    public int getApplication() {
        return application;
    }

    public void setApplication(int application) {
        this.application = application;
    }

    public String getBankAddress() {
        return bankAddress;
    }

    public void setBankAddress(String bankAddress) {
        this.bankAddress = bankAddress;
    }
}
