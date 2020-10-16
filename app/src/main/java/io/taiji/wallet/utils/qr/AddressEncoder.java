package io.taiji.wallet.utils.qr;

import java.io.IOException;
import java.math.BigInteger;

public class AddressEncoder {

    private String address;
    private String amount;
    private String data;

    public AddressEncoder(String address, String amount) {
        this(address);
        this.amount = amount;
    }

    public AddressEncoder(String address) {
        this.address = address;
    }

    public static AddressEncoder decode(String s) throws IOException {
        return decodeERC(s);
    }

    public static AddressEncoder decodeERC(String s) throws IOException {
        if (!s.startsWith("taiji:") && !s.startsWith("TAIJI:"))
            throw new IOException("Invalid data format");
        AddressEncoder re = new AddressEncoder(s.substring(6, 46));
        if(s.length() == 46) return re;
        String[] parsed = s.substring(47).split("\\?");
        for (String entry : parsed) {
            String[] entry_s = entry.split("=");
            if (entry_s.length != 2) continue;
            if (entry_s[0].equalsIgnoreCase("amount")) re.amount = entry_s[1];
            if (entry_s[0].equalsIgnoreCase("data")) re.data = entry_s[1];
        }
        return re;
    }

    public static String encodeERC(AddressEncoder a) {
        String re = "taiji:" + a.address;
        if (a.amount != null) re += "?amount=" + a.amount;
        if (a.data != null) re += "?data=" + a.data;
        return re;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
