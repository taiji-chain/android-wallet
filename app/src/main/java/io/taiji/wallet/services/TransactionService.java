package io.taiji.wallet.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;
import com.networknt.taiji.crypto.Credentials;
import com.networknt.taiji.crypto.LedgerEntry;
import com.networknt.taiji.crypto.RawTransaction;
import com.networknt.taiji.crypto.SignedTransaction;
import com.networknt.taiji.crypto.TransactionManager;

import java.io.IOException;
import java.math.BigInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import io.taiji.wallet.R;
import io.taiji.wallet.activities.MainActivity;
import io.taiji.wallet.network.TaijiAPI;
import io.taiji.wallet.utils.WalletStorage;

public class TransactionService extends IntentService {
    private static final String TAG = "TransactionService";

    private NotificationCompat.Builder builder;
    final int mNotificationId = 153;

    public TransactionService() {
        super("Transaction Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sendNotification();
        try {
            String fromAddress = intent.getStringExtra("FROM_ADDRESS");
            final String toAddress = intent.getStringExtra("TO_ADDRESS");
            final Long amount = intent.getLongExtra("AMOUNT", 0);
            final Long bankFee = intent.getLongExtra("BANK_FEE", 0);
            final String bankAddress = intent.getStringExtra("BANK_ADDRESS");
            String password = intent.getStringExtra("PASSWORD");
            String currency = intent.getStringExtra("CURRENCY");
            final Credentials credentials = WalletStorage.getInstance(getApplicationContext()).getFullWallet(getApplicationContext(), password, fromAddress);
            LedgerEntry feeEntry = new LedgerEntry(bankAddress, bankFee);
            LedgerEntry ledgerEntry = new LedgerEntry(toAddress, amount);
            RawTransaction rtx = new RawTransaction(currency);
            rtx.addCreditEntry(toAddress, ledgerEntry);
            rtx.addDebitEntry(credentials.getAddress(), ledgerEntry);
            rtx.addCreditEntry(bankAddress, feeEntry);
            rtx.addDebitEntry(credentials.getAddress(), feeEntry);
            SignedTransaction stx = TransactionManager.signTransaction(rtx, credentials);
            String res = TaijiAPI.getInstance().postTx(fromAddress, stx);
            Log.i(TAG, "response = " + res);
            if(res != null) {
                // there must be an error here.
                JSONObject object = new JSONObject(res);
                error(object.toString());
            } else {
                result("200", "SUC10200", "OK", "The request is handled successfully");
            }
        } catch (Exception e) {
            e.printStackTrace();
            error("Invalid Wallet Password!");
        }
    }

    private void result(String statusCode, String code, String message, String description) {
        builder
                .setContentTitle(getString(R.string.notification_transfersuc))
                .setProgress(100, 100, false)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentText(description);

        Intent main = new Intent(this, MainActivity.class);
        main.putExtra("STATUS", statusCode);
        main.putExtra("DESC", description);


        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                main, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, builder.build());
    }

    private void error(String err) {
        builder
                .setContentTitle(getString(R.string.notification_transferfail))
                .setProgress(100, 100, false)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentText(err);

        Intent main = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                main, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, builder.build());
    }

    private void sendNotification() {
        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(0x2d435c)
                .setTicker(getString(R.string.notification_transferingticker))
                .setContentTitle(getString(R.string.notification_transfering_title))
                .setContentText(getString(R.string.notification_check_notifiication))
                .setOngoing(true)
                .setProgress(0, 0, true);
        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, builder.build());
    }

}
