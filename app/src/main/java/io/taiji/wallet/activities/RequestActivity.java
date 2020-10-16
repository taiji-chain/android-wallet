package io.taiji.wallet.activities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.networknt.taiji.utility.Converter;

import java.util.ArrayList;
import java.util.List;

import io.taiji.wallet.R;
import io.taiji.wallet.data.UnitEntry;
import io.taiji.wallet.data.WalletDisplay;
import io.taiji.wallet.interfaces.StorableWallet;
import io.taiji.wallet.utils.AddressNameConverter;
import io.taiji.wallet.utils.UnitCalculator;
import io.taiji.wallet.utils.WalletAdapter;
import io.taiji.wallet.utils.WalletStorage;
import io.taiji.wallet.utils.qr.AddressEncoder;
import io.taiji.wallet.utils.qr.Contents;
import io.taiji.wallet.utils.qr.QREncoder;

import static io.taiji.wallet.R.id.qrcode;

public class RequestActivity extends SecureAppCompatActivity implements View.OnClickListener {

    private CoordinatorLayout coord;
    private ImageView qr;
    private RecyclerView recyclerView;
    private WalletAdapter walletAdapter;
    private List<WalletDisplay> wallets = new ArrayList<>();
    private String selectedTaijiAddress;
    private TextView amount;
    private TextView taijiRequested;
    private Spinner currencySpinner;
    private Long curAmount = 0L; // in shell
    private Converter.Unit unit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coord = (CoordinatorLayout) findViewById(R.id.main_content);
        qr = (ImageView) findViewById(qrcode);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        amount = (TextView) findViewById(R.id.amount);
        taijiRequested = (TextView) findViewById(R.id.taijiRequested);

        walletAdapter = new WalletAdapter(wallets, this, this, this);
        LinearLayoutManager mgr = new LinearLayoutManager(this.getApplicationContext());
        RecyclerView.LayoutManager mLayoutManager = mgr;
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(walletAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                mgr.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        amount.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    try {
                        updateAmount(amount.getText().toString());
                        updateQR();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        currencySpinner = (Spinner)findViewById(R.id.currency_spinner);
        List<String> unitList = new ArrayList<>();
        for(UnitEntry entry: UnitCalculator.getInstance().getConversionNames()) {
            unitList.add(entry.getName());
        }
        ArrayAdapter<String> curAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unitList);
        curAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(curAdapter);
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                unit = Converter.Unit.fromString(unitList.get(i));
                updateAmount(amount.getText().toString());
                update();
                updateQR();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        update();
        updateQR();
    }


    public void update() {
        wallets.clear();
        ArrayList<WalletDisplay> myAddresses = new ArrayList<WalletDisplay>();
        ArrayList<StorableWallet> storedAddresses = new ArrayList<StorableWallet>(WalletStorage.getInstance(this).get());
        for (int i = 0; i < storedAddresses.size(); i++) {
            if (i == 0) selectedTaijiAddress = storedAddresses.get(i).getPubKey();
            myAddresses.add(new WalletDisplay(
                    AddressNameConverter.getInstance(this).get(storedAddresses.get(i).getPubKey()),
                    storedAddresses.get(i).getPubKey()
            ));
        }

        wallets.addAll(myAddresses);
        walletAdapter.notifyDataSetChanged();
    }

    public void snackError(String s) {
        if (coord == null) return;
        Snackbar mySnackbar = Snackbar.make(coord, s, Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }

    public void updateQR() {
        int qrCodeDimention = 400;
        String iban = "iban:" + selectedTaijiAddress;
        if (curAmount > 0) {
            iban += "?amount=" + curAmount;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        QREncoder qrCodeEncoder;
        if (prefs.getBoolean("qr_encoding_erc", true)) {
            AddressEncoder temp = new AddressEncoder(selectedTaijiAddress);
            if (curAmount > 0)
                temp.setAmount(curAmount.toString());
            qrCodeEncoder = new QREncoder(AddressEncoder.encodeERC(temp), null,
                    Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention);
        } else {
            qrCodeEncoder = new QREncoder(iban, null,
                    Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention);
        }

        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            qr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
    private void updateAmount(String str) {
        try {
            final Long origA = new Long(str);
            curAmount = Converter.toShell(origA, unit);
        } catch (NumberFormatException e) {
            curAmount = 0L;
        }
        taijiRequested.setText(curAmount.toString());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View view) {
        int itemPosition = recyclerView.getChildLayoutPosition(view);
        selectedTaijiAddress = wallets.get(itemPosition).getPublicKey();
        updateQR();
    }
}

