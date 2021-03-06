package io.taiji.wallet.activities;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;


import com.networknt.taiji.crypto.Credentials;

import io.taiji.wallet.R;
import io.taiji.wallet.utils.WalletStorage;
import io.taiji.wallet.utils.qr.Contents;
import io.taiji.wallet.utils.qr.QREncoder;

import static io.taiji.wallet.R.id.qrcode;

public class PrivateKeyActivity extends SecureAppCompatActivity {

    private ImageView qr;
    private TextView privateKey;
    private ProgressBar progress;

    public static final String ADDRESS = "ADDRESS";
    public static final String PASSWORD = "PASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privatekey);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        qr = (ImageView) findViewById(qrcode);
        privateKey = (TextView) findViewById(R.id.privateKey);
        progress = (ProgressBar) findViewById(R.id.progressBar);

        getPrivateKey(getIntent().getStringExtra(PASSWORD), getIntent().getStringExtra(ADDRESS));
    }

    private void getPrivateKey(String password, String address) {
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... params) {
                try {
                    Credentials keys = WalletStorage.getInstance(getApplicationContext()).getFullWallet(getApplicationContext(), params[0], params[1]);
                    return keys.getEcKeyPair().getPrivateKey().toString(16);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                progress.setVisibility(View.GONE);
                qr.setVisibility(View.VISIBLE);
                update(result);
            }
        }.execute(password, address);

    }

    public void update(String key) {
        if (key != null) {
            privateKey.setText(key);
            updateQR(key);
        } else {
            privateKey.setText(getString(R.string.activity_private_key_wrong_pw));
            // Wrong key
        }
    }

    public void updateQR(String privateKey) {
        int qrCodeDimention = 400;
        QREncoder qrCodeEncoder = new QREncoder(privateKey, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention);

        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            qr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}

