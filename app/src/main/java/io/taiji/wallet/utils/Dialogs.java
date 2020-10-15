package io.taiji.wallet.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import me.grantland.widget.AutofitTextView;
import io.taiji.wallet.R;
import io.taiji.wallet.activities.AddressDetailActivity;
import io.taiji.wallet.activities.MainActivity;
import io.taiji.wallet.activities.WalletGenActivity;
import io.taiji.wallet.data.TokenDisplay;
import io.taiji.wallet.data.TransactionDisplay;
import io.taiji.wallet.data.WatchWallet;
import io.taiji.wallet.fragments.FragmentWallets;
import io.taiji.wallet.interfaces.AdDialogResponseHandler;
import io.taiji.wallet.interfaces.PasswordDialogCallback;

public class Dialogs {

    public static void askForPasswordAndDecode(Activity ac, final String fromAddress, final PasswordDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ac, R.style.AlertDialogTheme);
        builder.setTitle("Wallet Password");

        final EditText input = new EditText(ac);
        final CheckBox showpw = new CheckBox(ac);
        showpw.setText(R.string.password_in_clear_text);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());

        LinearLayout container = new LinearLayout(ac);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = ac.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.topMargin = ac.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.bottomMargin = ac.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = ac.getResources().getDimensionPixelSize(R.dimen.dialog_margin);

        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params2.leftMargin = ac.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params2.rightMargin = ac.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        input.setLayoutParams(params);
        showpw.setLayoutParams(params2);

        container.addView(input);
        container.addView(showpw);
        builder.setView(container);

        showpw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked)
                    input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                else
                    input.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                input.setSelection(input.getText().length());
            }
        });

        builder.setView(container);
        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager inputMgr = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMgr = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
                callback.success(input.getText().toString());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMgr = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
                callback.canceled();
                dialog.cancel();
            }
        });

        builder.show();

    }

    /*
    public static void showTokenetails(final Activity c, final TokenDisplay tok) {
        MaterialDialog dialog = new MaterialDialog.Builder(c)
                .customView(R.layout.dialog_token_detail, true)
                .show();
        View view = dialog.getCustomView();
        ImageView contractIcon = (ImageView) view.findViewById(R.id.my_addressicon);
        TextView tokenname = (TextView) view.findViewById(R.id.walletname);
        AutofitTextView contractAddr = (AutofitTextView) view.findViewById(R.id.walletaddr);

        TextView supply = (TextView) view.findViewById(R.id.supply);
        TextView priceUSD = (TextView) view.findViewById(R.id.price);
        TextView priceETH = (TextView) view.findViewById(R.id.price2);
        TextView capUSD = (TextView) view.findViewById(R.id.cap);
        TextView capETH = (TextView) view.findViewById(R.id.cap2);
        TextView holders = (TextView) view.findViewById(R.id.holders);
        TextView digits = (TextView) view.findViewById(R.id.digits);

        LinearLayout from = (LinearLayout) view.findViewById(R.id.from);

        from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(c, AddressDetailActivity.class);
                i.putExtra("ADDRESS", tok.getContractAddr());
                c.startActivity(i);
            }
        });

        ExchangeCalculator ex = ExchangeCalculator.getInstance();
        contractIcon.setImageBitmap(Blockies.createIcon(tok.getContractAddr()));
        tokenname.setText(tok.getName());
        contractAddr.setText(tok.getContractAddr());
        supply.setText(ex.displayUsdNicely(tok.getTotalSupplyLong()) + " " + tok.getShorty());
        priceUSD.setText(tok.getUsdprice() + " $");

        priceETH.setText(ex.displayEthNicelyExact(
                ex.convertTokenToEther(1, tok.getUsdprice())
        ) + " " + ex.getEtherCurrency().getShorty());
        capETH.setText(ex.displayUsdNicely(
                ex.convertTokenToEther(tok.getTotalSupplyLong(), tok.getUsdprice())
        ) + " " + ex.getEtherCurrency().getShorty());
        capUSD.setText(ex.displayUsdNicely(tok.getUsdprice() * tok.getTotalSupplyLong()) + " $");
        holders.setText(ex.displayUsdNicely(tok.getHolderCount()) + "");
        digits.setText(tok.getDigits() + "");
    }
    */
    public static void showTXDetails(final Activity c, final TransactionDisplay tx) {
        MaterialDialog dialog = new MaterialDialog.Builder(c)
                .customView(R.layout.dialog_tx_detail, true)
                .show();
        View view = dialog.getCustomView();
        ImageView myicon = (ImageView) view.findViewById(R.id.my_addressicon);
        ImageView othericon = (ImageView) view.findViewById(R.id.other_addressicon);
        TextView myAddressname = (TextView) view.findViewById(R.id.walletname);
        TextView otherAddressname = (TextView) view.findViewById(R.id.other_address);
        AutofitTextView myAddressaddr = (AutofitTextView) view.findViewById(R.id.walletaddr);
        AutofitTextView otherAddressaddr = (AutofitTextView) view.findViewById(R.id.other_addressaddr);

        TextView amount = (TextView) view.findViewById(R.id.amount);
        TextView month = (TextView) view.findViewById(R.id.month);
        TextView txid = (TextView) view.findViewById(R.id.txid);
        Button openInBrowser = (Button) view.findViewById(R.id.openinbrowser);
        LinearLayout from = (LinearLayout) view.findViewById(R.id.from);
        LinearLayout to = (LinearLayout) view.findViewById(R.id.to);

        from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(c, AddressDetailActivity.class);
                i.putExtra("ADDRESS", tx.getFromAddress());
                c.startActivity(i);
            }
        });

        to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(c, AddressDetailActivity.class);
                i.putExtra("ADDRESS", tx.getToAddress());
                c.startActivity(i);
            }
        });

        openInBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://test.taiji.io/tx/" + tx.getId();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                c.startActivity(i);
            }
        });

        myicon.setImageBitmap(Blockies.createIcon(tx.getFromAddress()));
        othericon.setImageBitmap(Blockies.createIcon(tx.getToAddress()));

        String myName = AddressNameConverter.getInstance(c).get(tx.getFromAddress());
        if (myName == null) myName = shortName(tx.getFromAddress());
        String otherName = AddressNameConverter.getInstance(c).get(tx.getToAddress());
        if (otherName == null) otherName = shortName(tx.getToAddress());
        myAddressname.setText(myName);
        otherAddressname.setText(otherName);

        myAddressaddr.setText(tx.getFromAddress());
        otherAddressaddr.setText(tx.getToAddress());
        SimpleDateFormat dateformat = new SimpleDateFormat("dd. MMMM yyyy, HH:mm:ss", Locale.getDefault());
        month.setText(dateformat.format(tx.getTimestamp()) + "");
        txid.setText(tx.getId());
        amount.setText(tx.getType() + tx.getAmount() + " SH");
        amount.setTextColor(c.getResources().getColor(tx.getType().equals("+") ? R.color.taijiReceived : R.color.taijiSpent));
    }

    private static String shortName(String addr) {
        return addr.substring(4, 10);
    }

    public static void addWatchOnly(final MainActivity c) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.dialog_watch_only_title);

        final EditText input = new EditText(c);
        input.setSingleLine();
        FrameLayout container = new FrameLayout(c);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.topMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.bottomMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        input.setLayoutParams(params);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        container.addView(input);
        builder.setView(container);
        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager inputMgr = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        });
        builder.setNegativeButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                InputMethodManager inputMgr = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
                if (input.getText().toString().length() == 42 && input.getText().toString().startsWith("0x")) {
                    final boolean suc = WalletStorage.getInstance(c).add(new WatchWallet(input.getText().toString()), c);
                    new Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (c.fragments != null && c.fragments[1] != null) {
                                        try {
                                            ((FragmentWallets) c.fragments[1]).update();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    c.snackError(c.getResources().getString(suc ? R.string.main_ac_wallet_added_suc : R.string.main_ac_wallet_added_er));
                                    if (suc)
                                        AddressNameConverter.getInstance(c).put(input.getText().toString(), "Watch " + input.getText().toString().substring(0, 6), c);
                                }
                            }, 100);
                } else {
                    c.snackError("Invalid Taiji address!");
                }
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.show, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMgr = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
                if (input.getText().toString().length() == 42 && input.getText().toString().startsWith("0x")) {
                    Intent detail = new Intent(c, AddressDetailActivity.class);
                    detail.putExtra("ADDRESS", input.getText().toString());
                    c.startActivity(detail);
                } else {
                    c.snackError("Invalid Taiji address!");
                }
                dialog.cancel();
            }
        });
        builder.setNeutralButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMgr = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
                dialog.cancel();
            }
        });

        builder.show();

    }

    public static void writeDownPassword(final WalletGenActivity c) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.dialog_write_down_pw_title);
        builder.setMessage(c.getString(R.string.dialog_write_down_pw_text));
        builder.setPositiveButton(R.string.action_sign_in, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                c.startWalletGenRunnerService();
                dialog.cancel();
            }
        });
        builder.setNegativeButton(R.string.dialog_back_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public static void importWallets(final MainActivity c, final ArrayList<File> files) {
        String addresses = "";
        for (int i = 0; i < files.size() && i < 3; i++)
            addresses += WalletStorage.stripWalletName(files.get(i).getName()) + "\n";

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.dialog_importing_wallets_title);
        builder.setMessage(String.format(c.getString(R.string.dialog_importing_wallets_text), files.size(), files.size() > 1 ? "s" : "", addresses));
        builder.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    WalletStorage.getInstance(c).importWallets(c, files);
                    c.snackError("Wallet" + (files.size() > 1 ? "s" : "") + " successfully imported!");
                    if (c.fragments != null && c.fragments[1] != null)
                        ((FragmentWallets) c.fragments[1]).update();
                } catch (Exception e) {
                    c.snackError("Error while importing wallets");
                    e.printStackTrace();
                }
                dialog.cancel();
            }
        });
        builder.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public static void adDisable(Context c, final AdDialogResponseHandler res) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.dialog_disable_ad_title);
        builder.setMessage(R.string.dialog_disable_ad_text);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.fragment_recipient_continue, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                res.continueSettingChange(true);
            }
        });
        builder.setNegativeButton(R.string.dialog_back_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                res.continueSettingChange(false);
            }
        });

        builder.show();
    }

    public static void cantExportNonWallet(Context c) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.dialog_ex_nofull_title);
        builder.setMessage(R.string.dialog_ex_nofull_text);
        builder.setNeutralButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public static void exportWallet(Context c, DialogInterface.OnClickListener yes) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.dialog_exporting_title);
        builder.setMessage(R.string.dialog_exporting_text);
        builder.setPositiveButton(R.string.button_ok, yes);
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static void noFullWallet(Context c) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.dialog_nofullwallet);
        builder.setMessage(R.string.dialog_nofullwallet_text);
        builder.setNeutralButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public static void noWallet(Context c) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.dialog_no_wallets);
        builder.setMessage(R.string.dialog_no_wallets_text);
        builder.setNeutralButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public static void noImportWalletsFound(Context c) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.dialog_no_wallets_found);
        builder.setMessage(R.string.dialog_no_wallets_found_text);
        builder.setNeutralButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
