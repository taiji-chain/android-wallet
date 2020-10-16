package io.taiji.wallet.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import io.taiji.wallet.interfaces.LastIconLoaded;
import io.taiji.wallet.interfaces.StorableWallet;
import io.taiji.wallet.utils.Key;
import io.taiji.wallet.utils.RequestCache;
import io.taiji.wallet.utils.TokenIconCache;

public class TaijiAPI {

    private String token;

    private static TaijiAPI instance;

    public static TaijiAPI getInstance() {
        if (instance == null)
            instance = new TaijiAPI();
        return instance;
    }

    /**
     * Retrieve all normal ether transactions from address (excluding contract calls etc, @see rehanced.com.simpleetherwallet.network.EtherscanAPI#getInternalTransactions() )
     *
     * @param address Ether address
     * @param b       Network callback to @see rehanced.com.simpleetherwallet.fragments.FragmentTransactions#update() or @see rehanced.com.simpleetherwallet.fragments.FragmentTransactionsAll#update()
     * @param force   Whether to force (true) a network call or use cache (false). Only true if user uses swiperefreshlayout
     * @throws IOException Network exceptions
     */
    public void getTransactions(String address, Callback b, boolean force) throws IOException {
        String url = "https://test.taiji.io/transaction/" + address + "/taiji?offset=0&limit=10";
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
                    .url("https://test.taiji.io/token/account/" + address)
                    .build()).protocol(Protocol.HTTP_1_0).body(ResponseBody.create(MediaType.parse("JSON"), RequestCache.getInstance().get(RequestCache.TYPE_TOKENS, address))).build());
            return;
        }
        get("https://test.taiji.io/token/account/" + address, b);
    }

    public void getFee(String address, Callback b) throws IOException {
        if (RequestCache.getInstance().contains(RequestCache.TYPE_FEES, address.substring(0, 4))) {
            b.onResponse(null, new Response.Builder().code(200).message("").request(new Request.Builder()
                    .url("https://test.taiji.io/fee/taiji")
                    .build()).protocol(Protocol.HTTP_2).body(ResponseBody.create(MediaType.parse("JSON"), RequestCache.getInstance().get(RequestCache.TYPE_FEES, address.substring(0, 4)))).build());
            return;
        }
        get("https://test.taiji.io/fee/taiji", b);
    }

    /**
     * Download and save token icon in permanent image cache (TokenIconCache)
     *
     * @param c         Application context, used to load TokenIconCache if reinstanced
     * @param tokenName Name of token
     * @param lastToken Boolean defining whether this is the last icon to download or not. If so callback is called to refresh recyclerview (notifyDataSetChanged)
     * @param callback  Callback to @see rehanced.com.simpleetherwallet.fragments.FragmentDetailOverview#onLastIconDownloaded()
     * @throws IOException Network exceptions
     */
    public void loadTokenIcon(final Context c, String tokenName, final boolean lastToken, final LastIconLoaded callback) throws IOException {
        if (tokenName.indexOf(" ") > 0)
            tokenName = tokenName.substring(0, tokenName.indexOf(" "));
        if (TokenIconCache.getInstance(c).contains(tokenName)) return;

        if(tokenName.equalsIgnoreCase("OMGToken"))
            tokenName = "omise";
        else if(tokenName.equalsIgnoreCase("0x"))
            tokenName = "0xtoken_28";

        final String tokenNamef = tokenName;
        get("https://etherscan.io//token/images/" + tokenNamef + ".PNG", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (c == null) return;
                ResponseBody in = response.body();
                InputStream inputStream = in.byteStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                final Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream);
                TokenIconCache.getInstance(c).put(c, tokenNamef, new BitmapDrawable(c.getResources(), bitmap).getBitmap());
                // if(lastToken) // TODO: resolve race condition
                callback.onLastIconDownloaded();
            }
        });
    }



    public void getBalance(String address, Callback b) throws IOException {
        get("https://test.taiji.io/account/" + address, b);
    }


    public void getNonceForAddress(String address, Callback b) throws IOException {
        get("https://api.etherscan.io/api?module=proxy&action=eth_getTransactionCount&address=" + address + "&tag=latest&apikey=" + token, b);
    }


    public void getBalances(ArrayList<StorableWallet> addresses, Callback b) throws IOException {
        String url = "https://test.taiji.io/account?addresses=";
        for (StorableWallet address : addresses)
            url += address.getPubKey() + ",";
        url = url.substring(0, url.length() - 1); // remove last ,
        Log.i("TAG", "url = " + url);
        get(url, b);
    }


    public void forwardTransaction(String raw, Callback b) throws IOException {
        get("https://api.etherscan.io/api?module=proxy&action=eth_sendRawTransaction&hex=" + raw + "&apikey=" + token, b);
    }


    public void get(String url, Callback b) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        client.newCall(request).enqueue(b);
    }


    private TaijiAPI() {
        token = new Key("").toString();
    }

}
