package io.taiji.wallet.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.networknt.taiji.utility.Converter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.taiji.wallet.data.BankFee;
import io.taiji.wallet.data.UnitEntry;
import io.taiji.wallet.utils.RequestCache;
import io.taiji.wallet.utils.UnitCalculator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import io.taiji.wallet.BuildConfig;
import io.taiji.wallet.R;
import io.taiji.wallet.activities.SendActivity;
import io.taiji.wallet.interfaces.PasswordDialogCallback;
import io.taiji.wallet.network.TaijiAPI;
import io.taiji.wallet.services.TransactionService;
import io.taiji.wallet.utils.AddressNameConverter;
import io.taiji.wallet.utils.Blockies;
import io.taiji.wallet.utils.Dialogs;
import io.taiji.wallet.utils.ResponseParser;
import io.taiji.wallet.utils.WalletStorage;

import static android.app.Activity.RESULT_OK;

public class FragmentSend extends Fragment {
    private SendActivity ac;
    private Button send;
    private EditText amount;
    private TextView toAddress, toName, fromName;
    private TextView availableTaiji;
    private TextView txCost;
    private TextView totalCost;
    private ImageView toicon, fromicon;
    private Spinner spinner;
    private Spinner currencySpinner;
    private Long curAmount = 0L; // in shell
    private Long curTxCost = 1000L; // in shell
    private Converter.Unit unit;
    private Long curAvailable = 0L; // in shell
    private EditText data;
    private BankFee bankFee;

    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_send, container, false);

        ac = (SendActivity) this.getActivity();

        send = (Button) rootView.findViewById(R.id.send);
        amount = (EditText) rootView.findViewById(R.id.amount);
        toAddress = (TextView) rootView.findViewById(R.id.toAddress);
        toName = (TextView) rootView.findViewById(R.id.toName);
        fromName = (TextView) rootView.findViewById(R.id.fromName);

        availableTaiji = (TextView) rootView.findViewById(R.id.taijiAvailable);

        txCost = (TextView) rootView.findViewById(R.id.txCost);
        totalCost = (TextView) rootView.findViewById(R.id.totalCost);

        toicon = (ImageView) rootView.findViewById(R.id.toicon);
        fromicon = (ImageView) rootView.findViewById(R.id.fromicon);
        data = (EditText) rootView.findViewById(R.id.data);

        if (getArguments().containsKey("TO_ADDRESS")) {
            setToAddress(getArguments().getString("TO_ADDRESS"), ac);
        }

        if (getArguments().containsKey("AMOUNT")) {
            curAmount = new Long(getArguments().getString("AMOUNT"));
            amount.setText(getArguments().getString("AMOUNT"));
        }

        spinner = (Spinner) rootView.findViewById(R.id.spinner);
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(ac, R.layout.address_spinner, WalletStorage.getInstance(ac).getFullOnly()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setPadding(0, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                return view;
            }
        };
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateAccountBalance();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateAmount(s.toString());
                updateDisplays();
            }
        });

        currencySpinner = (Spinner) rootView.findViewById(R.id.currency_spinner);
        List<String> unitList = new ArrayList<>();
        for(UnitEntry entry: UnitCalculator.getInstance().getConversionNames()) {
            unitList.add(entry.getName());
        }
        ArrayAdapter<String> curAdapter = new ArrayAdapter<>(ac, android.R.layout.simple_spinner_item, unitList);
        curAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(curAdapter);
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("TAG", "i = " + i + " l = " + l);
                Log.i("TAG", "unit name = " + unitList.get(i));
                unit = Converter.Unit.fromString(unitList.get(i));
                Log.i("TAG", "unit = " + unit.toString());
                updateAmount(amount.getText().toString());
                updateDisplays();
                getFee();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((amount.getText().length() <= 0 || new BigDecimal(amount.getText().toString()).compareTo(new BigDecimal("0")) <= 0) && data.getText().length() <= 0) {
                    ac.snackError(getString(R.string.err_send_noamount));
                    return;
                }
                if (toAddress == null || toAddress.getText().length() == 0) {
                    ac.snackError(getString(R.string.err_send_noreceiver));
                    return;
                }
                if (spinner == null || spinner.getSelectedItem() == null) return;
                try {
                    if (BuildConfig.DEBUG)
                        Log.d("taijiBalance", (getCurTotalCost().compareTo(curAvailable) < 0) + " | " + getCurTotalCost() + " | " + curAvailable + " | " + curAmount);
                    if (getCurTotalCost().compareTo(curAvailable) < 0 || BuildConfig.DEBUG || data.getText().length() > 0) {
                        Dialogs.askForPasswordAndDecode(ac, spinner.getSelectedItem().toString(), new PasswordDialogCallback(){

                            @Override
                            public void success(String password) {
                                sendTaiji(password, spinner.getSelectedItem().toString());
                            }

                            @Override
                            public void canceled() {}
                        });
                    } else {
                        ac.snackError(getString(R.string.err_send_not_enough_taiji));
                    }
                } catch (Exception e) {
                    ac.snackError(getString(R.string.err_send_invalidamount));
                }

            }
        });

        if (getArguments().containsKey("FROM_ADDRESS")) {
            setFromAddress(getArguments().getString("FROM_ADDRESS"));
        }

        updateAccountBalance();
        updateDisplays();

        return rootView;
    }

    private void updateAccountBalance() {
        try {
            TaijiAPI.getInstance().getBalance(spinner.getSelectedItem().toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ac.snackError("Cant fetch your account balance", Snackbar.LENGTH_LONG);
                        }
                    });

                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                curAvailable = new Long(ResponseParser.parseBalance(response.body().string()));
                                updateDisplays();
                            } catch (Exception e) {
                                ac.snackError("Cant fetch your account balance");
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        fromicon.setImageBitmap(Blockies.createIcon(spinner.getSelectedItem().toString()));
        fromName.setText(AddressNameConverter.getInstance(ac).get(spinner.getSelectedItem().toString()));
    }

    private void setFromAddress(String from) {
        ArrayList<String> fullwallets = WalletStorage.getInstance(ac).getFullOnly();
        for (int i = 0; i < fullwallets.size(); i++) {
            if (fullwallets.get(i).equalsIgnoreCase(from)) {
                spinner.setSelection(i);
            }
        }
    }

    private void updateDisplays() {
        updateAvailableDisplay();
        updateTxCostDisplay();
        updateTotalCostDisplay();
    }

    private void updateAvailableDisplay() {
        availableTaiji.setText(curAvailable.toString());
    }

    private void updateAmount(String str) {
        try {
            final Long origA = new Long(str);
            curAmount = Converter.toShell(origA, unit);
        } catch (NumberFormatException e) {
            curAmount = 0L;
        }
    }

    private void updateTxCostDisplay() {
        txCost.setText(curTxCost.toString());
    }

    private Long getCurTotalCost() {
        return curAmount + curTxCost;
    }

    private void updateTotalCostDisplay() {
        // total cost in SHELL
        final Long curTotalCost = getCurTotalCost();
        totalCost.setText(curTotalCost.toString());
    }

    private void getFee() {
        try {
            TaijiAPI.getInstance().getFee(spinner.getSelectedItem().toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String restring = response.body().string();
                    Log.i("TAG", restring);
                    if (restring != null && restring.length() > 2) {
                        RequestCache.getInstance().put(RequestCache.TYPE_FEES, spinner.getSelectedItem().toString().substring(0, 4), restring);
                    }
                    try {
                        bankFee = ResponseParser.parseBankFee(restring);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(spinner.getSelectedItem().toString().substring(0, 4).equals(toAddress.toString().substring(0, 4))) {
                                curTxCost = (long)bankFee.getInnerChain();
                            } else {
                                curTxCost = (long)bankFee.getInterChain();
                            }
                            txCost.setText(curTxCost.toString());
                        }
                    });
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void sendTaiji(String password, String fromAddress) {
        Intent txService = new Intent(ac, TransactionService.class);
        txService.putExtra("CURRENCY", "taiji");
        txService.putExtra("FROM_ADDRESS", fromAddress);
        txService.putExtra("TO_ADDRESS", toAddress.getText().toString());
        txService.putExtra("AMOUNT", curAmount);
        txService.putExtra("BANK_FEE", curTxCost);
        txService.putExtra("PASSWORD", password);
        txService.putExtra("BANK_ADDRESS", bankFee.getBankAddress());
        ac.startService(txService);

        Intent data = new Intent();
        data.putExtra("FROM_ADDRESS", fromAddress);
        data.putExtra("TO_ADDRESS", toAddress.getText().toString());
        data.putExtra("AMOUNT", curAmount);
        ac.setResult(RESULT_OK, data);
        ac.finish();
    }

    public void setToAddress(String to, Context c) {
        if (toAddress == null) return;
        toAddress.setText(to);
        String name = AddressNameConverter.getInstance(c).get(to);
        toName.setText(name == null ? to.substring(0, 10) : name);
        toicon.setImageBitmap(Blockies.createIcon(to));
        getFee();
    }
}