package io.taiji.wallet.network;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.networknt.taiji.crypto.SignedTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.taiji.wallet.WalletApplication;
import io.taiji.wallet.interfaces.StorableWallet;
import io.taiji.wallet.utils.OwnWalletUtils;
import io.taiji.wallet.utils.RequestCache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class TaijiAPI {

    private String server;

    private static TaijiAPI instance;

    public static TaijiAPI getInstance() {
        if (instance == null)
            instance = new TaijiAPI();
        return instance;
    }

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public String postTx(String address, SignedTransaction stx) {
        String url = server + "/tx";
        String bankId = address.substring(0, 4);
        try {
            String s = OwnWalletUtils.objectMapper.writeValueAsString(stx);
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), s);
            Request request = new Request.Builder()
            .url(url)
            .addHeader("env_tag", bankId)
            .post(body)
            .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("TAG", "Exception: " + e.getMessage());
            return e.getMessage();
        }
    }

    /**
     * @param address Taiji address
     * @param b       Network callback to @see
     * @param force   Whether to force (true) a network call or use cache (false). Only true if user uses swiperefreshlayout
     * @throws IOException Network exceptions
     */
    public void getTransactions(String address, Callback b, boolean force) throws IOException {
        String url = server + "/transaction/" + address + "/taiji?offset=0&limit=10";
        if (!force && RequestCache.getInstance().contains(RequestCache.TYPE_TXS, address)) {
            b.onResponse(null, new Response.Builder().code(200).message("").request(new Request.Builder()
                    .url(url)
                    .build()).protocol(Protocol.HTTP_2).body(ResponseBody.create(MediaType.parse("JSON"), RequestCache.getInstance().get(RequestCache.TYPE_TXS, address))).build());
            return;
        }
        Log.i("TAG", "get transactions " + url);
        get(url, b);
    }

    /**
     * Get token balances via token-reader service
     *
     * @param address Taiji address
     * @param b       callback function
     * @param force   Whether to force (true) a network call or use cache (false). Only true if user uses swiperefreshlayout
     * @throws IOException Network exceptions
     */
    public void getTokenBalances(String address, Callback b, boolean force) throws IOException {
        if (!force && RequestCache.getInstance().contains(RequestCache.TYPE_TOKENS, address)) {
            b.onResponse(null, new Response.Builder().code(200).message("").request(new Request.Builder()
                    .url(server + "/token/account/" + address)
                    .build()).protocol(Protocol.HTTP_1_0).body(ResponseBody.create(MediaType.parse("JSON"), RequestCache.getInstance().get(RequestCache.TYPE_TOKENS, address))).build());
            return;
        }
        get(server + "/token/account/" + address, b);
    }

    public void getFee(String address, Callback b) throws IOException {
        if (RequestCache.getInstance().contains(RequestCache.TYPE_FEES, address.substring(0, 4))) {
            b.onResponse(null, new Response.Builder().code(200).message("").request(new Request.Builder()
                    .url(server + "/fee/taiji")
                    .build()).protocol(Protocol.HTTP_2).body(ResponseBody.create(MediaType.parse("JSON"), RequestCache.getInstance().get(RequestCache.TYPE_FEES, address.substring(0, 4)))).build());
            return;
        }
        get(server + "/fee/taiji", b);
    }


    public void getBalance(String address, Callback b) throws IOException {
        get(server + "/account/" + address, b);
    }


    public void getNonceForAddress(String address, Callback b) throws IOException {
        get(server + "/api?module=proxy&action=eth_getTransactionCount&address=" + address, b);
    }


    public void getBalances(ArrayList<StorableWallet> addresses, Callback b) throws IOException {
        String url = server + "/account?addresses=";
        for (StorableWallet address : addresses)
            url += address.getPubKey() + ",";
        url = url.substring(0, url.length() - 1); // remove last ,
        Log.i("TAG", "url = " + url);
        get(url, b);
    }


    public void get(String url, Callback b) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(b);
    }


    private TaijiAPI() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WalletApplication.getAppContext());
        if(preferences.getBoolean("testnetSwitch", true)) {
            server = "https://test.taiji.io";
        } else {
            server = "https://taiji.io";
        }
    }

}
