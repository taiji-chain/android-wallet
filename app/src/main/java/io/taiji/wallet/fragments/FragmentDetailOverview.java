package io.taiji.wallet.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.appbar.AppBarLayout;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.taiji.wallet.activities.AddressDetailActivity;
import io.taiji.wallet.data.UnitEntry;
import io.taiji.wallet.utils.UnitCalculator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import io.taiji.wallet.R;
import io.taiji.wallet.activities.SendActivity;
import io.taiji.wallet.data.TokenDisplay;
import io.taiji.wallet.data.WatchWallet;
import io.taiji.wallet.interfaces.LastIconLoaded;
import io.taiji.wallet.network.TaijiAPI;
import io.taiji.wallet.utils.AddressNameConverter;
import io.taiji.wallet.utils.AppBarStateChangeListener;
import io.taiji.wallet.utils.Blockies;
import io.taiji.wallet.utils.Dialogs;
import io.taiji.wallet.utils.RequestCache;
import io.taiji.wallet.utils.ResponseParser;
import io.taiji.wallet.utils.TokenAdapter;
import io.taiji.wallet.utils.WalletStorage;

public class FragmentDetailOverview extends Fragment implements View.OnClickListener, View.OnCreateContextMenuListener, LastIconLoaded {
    private static final String TAG = FragmentDetailOverview.class.getSimpleName();

    private AddressDetailActivity ac;
    private String taijiAddress = "";
    private byte type;
    private TextView balance, address, currency;
    private ImageView icon;
    private LinearLayout header;
    private long balanceLong = 0L;
    private FloatingActionMenu fabmenu;
    private RecyclerView recyclerView;
    private TokenAdapter walletAdapter;
    private List<TokenDisplay> token = new ArrayList<>();
    private SwipeRefreshLayout swipeLayout;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail_ov, container, false);

        ac = (AddressDetailActivity) this.getActivity();
        taijiAddress = getArguments().getString("ADDRESS");
        type = getArguments().getByte("TYPE");

        icon = (ImageView) rootView.findViewById(R.id.addressimage);
        address = (TextView) rootView.findViewById(R.id.ethaddress);
        balance = (TextView) rootView.findViewById(R.id.balance);
        currency = (TextView) rootView.findViewById(R.id.currency);
        header = (LinearLayout) rootView.findViewById(R.id.header);
        fabmenu = (FloatingActionMenu) rootView.findViewById(R.id.fabmenu);

        UnitEntry unitEntry = UnitCalculator.getInstance().getCurrent();
        balanceLong = getArguments().getLong("BALANCE");
        balance.setText(UnitCalculator.getInstance().convertUnit(balanceLong, unitEntry.getUnit()) + "");
        currency.setText(unitEntry.getName());

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        walletAdapter = new TokenAdapter(token, ac, this, this);
        LinearLayoutManager mgr = new LinearLayoutManager(ac.getApplicationContext());
        RecyclerView.LayoutManager mLayoutManager = mgr;
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(walletAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), mgr.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout2);
        swipeLayout.setColorSchemeColors(ac.getResources().getColor(R.color.colorPrimary));
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    update(true);
                } catch (IOException e) {
                    if (ac != null)
                        ac.snackError("Connection problem");
                    e.printStackTrace();
                }
            }
        });

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UnitEntry cur = UnitCalculator.getInstance().next();
                balance.setText(UnitCalculator.getInstance().convertUnit(balanceLong, cur.getUnit()) + "");
                currency.setText(cur.getName());
                walletAdapter.notifyDataSetChanged();
                if (ac != null)
                    ac.broadCastDataSetChanged();
            }
        });

        icon.setImageBitmap(Blockies.createIcon(taijiAddress, 24));
        address.setText(taijiAddress);

        FloatingActionButton fab_setName = (FloatingActionButton) rootView.findViewById(R.id.set_name);
        fab_setName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setName();
            }
        });

        FloatingActionButton send_taiji = (FloatingActionButton) rootView.findViewById(R.id.send_taiji); // Send Taiji to
        send_taiji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (WalletStorage.getInstance(ac).getFullOnly().size() == 0) {
                    Dialogs.noFullWallet(ac);
                } else {
                    Intent tx = new Intent(ac, SendActivity.class);
                    tx.putExtra("TO_ADDRESS", taijiAddress);
                    ac.startActivityForResult(tx, SendActivity.REQUEST_CODE);
                }
            }
        });

        FloatingActionButton send_taiji_from = (FloatingActionButton) rootView.findViewById(R.id.send_taiji_from);
        send_taiji_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (WalletStorage.getInstance(ac).getFullOnly().size() == 0) {
                    Dialogs.noFullWallet(ac);
                } else {
                    Intent tx = new Intent(ac, SendActivity.class);
                    tx.putExtra("FROM_ADDRESS", taijiAddress);
                    ac.startActivityForResult(tx, SendActivity.REQUEST_CODE);
                }
            }
        });

        FloatingActionButton fab_add = (FloatingActionButton) rootView.findViewById(R.id.add_as_watch);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean suc = WalletStorage.getInstance(ac).add(new WatchWallet(taijiAddress), ac);
                new Handler().postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                ac.snackError(ac.getResources().getString(suc ? R.string.main_ac_wallet_added_suc : R.string.main_ac_wallet_added_er));
                            }
                        }, 100);
            }
        });

        if (type == AddressDetailActivity.OWN_WALLET) {
            fab_add.setVisibility(View.GONE);
        }
        if (!WalletStorage.getInstance(ac).isFullWallet(taijiAddress)) {
            send_taiji_from.setVisibility(View.GONE);
        }

        if (ac.getAppBar() != null) {
            ac.getAppBar().addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                public void onStateChanged(AppBarLayout appBarLayout, State state) {
                    if (state == State.COLLAPSED) {
                        fabmenu.hideMenu(true);
                    } else {
                        fabmenu.showMenu(true);
                    }
                }
            });
        }
        try {
            update(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rootView;
    }

    public void update(boolean force) throws IOException {
        token.clear();
        balanceLong = new Long("0");
        TaijiAPI.getInstance().getBalance(taijiAddress, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ac.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ac.snackError("Can't connect to network");
                        onItemsLoadComplete();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "response = " + response);
                long bal;
                try {
                    if(response.code() < 400) {
                        bal = new Long(ResponseParser.parseBalance(response.body().string()));
                    } else {
                        bal = 0L;
                    }
                    balanceLong = bal;
                } catch (JSONException e) {
                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onItemsLoadComplete();
                        }
                    });
                    e.printStackTrace();
                }
                final UnitEntry unitEntry = UnitCalculator.getInstance().getCurrent();
                ac.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        balance.setText(UnitCalculator.getInstance().convertUnit(balanceLong, unitEntry.getUnit()) + "");
                        currency.setText(unitEntry.getName());
                        walletAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        TaijiAPI.getInstance().getTokenBalances(taijiAddress, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ac.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ac.snackError("Can't connect to network");
                        onItemsLoadComplete();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    if(response.code() < 400) {
                        String restring = response.body().string();
                        if (restring != null && restring.length() > 2)
                            RequestCache.getInstance().put(RequestCache.TYPE_TOKENS, taijiAddress, restring);
                        token.addAll(ResponseParser.parseTokens(ac, restring, FragmentDetailOverview.this));
                    } else {
                        Log.i(TAG, "response = " + response);
                    }
                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            walletAdapter.notifyDataSetChanged();
                            onItemsLoadComplete();
                        }
                    });
                } catch (Exception e) {
                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onItemsLoadComplete();
                        }
                    });
                    //ac.snackError("Invalid server response");
                }
            }
        }, force);
    }

    public void setName() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(ac, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(ac);
        if (type == AddressDetailActivity.OWN_WALLET)
            builder.setTitle(R.string.name_your_address);
        else
            builder.setTitle(R.string.name_this_address);

        final EditText input = new EditText(ac);
        input.setText(AddressNameConverter.getInstance(ac).get(taijiAddress));
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setSingleLine();
        FrameLayout container = new FrameLayout(ac);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        input.setLayoutParams(params);
        input.setSelection(input.getText().length());

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
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMgr = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
                AddressNameConverter.getInstance(ac).put(taijiAddress, input.getText().toString(), ac);
                ac.setTitle(input.getText().toString());

            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMgr = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
                dialog.cancel();
            }
        });

        builder.show();
    }

    void onItemsLoadComplete() {
        if (swipeLayout == null) return;
        swipeLayout.setRefreshing(false);
    }

    @Override
    public void onClick(View view) {
        if (ac == null) return;
        int itemPosition = recyclerView.getChildLayoutPosition(view);
        if (itemPosition == 0 || itemPosition >= token.size()) return;  // if clicked on Taiji
        //Dialogs.showTokenetails(ac, token.get(itemPosition));
    }

    @Override
    public void onLastIconDownloaded() {
        if (walletAdapter != null && ac != null) {
            ac.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    walletAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}