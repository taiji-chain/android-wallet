package io.taiji.wallet.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;

import com.networknt.taiji.crypto.CipherException;
import com.networknt.taiji.crypto.ECKeyPair;

import org.bouncycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import io.taiji.wallet.R;
import io.taiji.wallet.activities.MainActivity;
import io.taiji.wallet.data.FullWallet;
import io.taiji.wallet.utils.AddressNameConverter;
import io.taiji.wallet.utils.Blockies;
import io.taiji.wallet.utils.OwnWalletUtils;
import io.taiji.wallet.utils.Settings;
import io.taiji.wallet.utils.WalletStorage;

public class WalletGenService extends IntentService {

    private NotificationCompat.Builder builder;
    final int mNotificationId = 152;

    private boolean normalMode = true;

    public WalletGenService() {
        super("WalletGen Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String password = intent.getStringExtra("PASSWORD");
        String chainId = intent.getStringExtra("BANK_ID");

        sendNotification();
        try {
            String walletAddress = OwnWalletUtils.generateNewWalletFile(password, new File(this.getFilesDir(), ""), chainId, true);
            WalletStorage.getInstance(this).add(new FullWallet(walletAddress, walletAddress), this);
            AddressNameConverter.getInstance(this).put(walletAddress, "Wallet " + walletAddress.substring(0, 6), this);
            Settings.walletBeingGenerated = false;
            finished(walletAddress);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    private void sendNotification() {
        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(0x2d435c)
                .setTicker(normalMode ? getString(R.string.notification_wallgen_title) : getString(R.string.notification_wallimp_title))
                .setContentTitle(this.getResources().getString(normalMode ? R.string.wallet_gen_service_title : R.string.wallet_gen_service_title_import))
                .setOngoing(true)
                .setProgress(0, 0, true)
                .setContentText(getString(R.string.notification_wallgen_maytake));
        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, builder.build());
    }

    private void finished(String address) {
        builder
                .setContentTitle(normalMode ? getString(R.string.notification_wallgen_finished) : getString(R.string.notification_wallimp_finished))
                .setLargeIcon(Blockies.createIcon(address.toLowerCase()))
                .setAutoCancel(true)
                .setLights(Color.CYAN, 3000, 3000)
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .setProgress(100, 100, false)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentText(getString(R.string.notification_click_to_view));

        if (android.os.Build.VERSION.SDK_INT >= 18) // Android bug in 4.2, just disable it for everyone then...
            builder.setVibrate(new long[]{1000, 1000});

        Intent main = new Intent(this, MainActivity.class);
        main.putExtra("STARTAT", 1);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                main, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, builder.build());
    }


}
