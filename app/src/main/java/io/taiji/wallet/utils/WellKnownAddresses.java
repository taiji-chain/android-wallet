package io.taiji.wallet.utils;


import java.util.HashMap;

public class WellKnownAddresses extends HashMap<String, String> {

    public WellKnownAddresses() {
        super();

        put("000085f904e427a2721738930681ca7AA2E7984B", "Bank 0000");
        put("00013e98C16bAAcD74b8EBD24c2911c299520e7a", "Bank 0001");
        put("0002502F06d625dcc335c3FC7F8B40B8cb3f1c9c", "Bank 0001");
    }

}
