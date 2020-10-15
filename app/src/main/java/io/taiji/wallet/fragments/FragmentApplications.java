package io.taiji.wallet.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import io.taiji.wallet.R;
import io.taiji.wallet.activities.MainActivity;
import io.taiji.wallet.network.TaijiAPI;
import io.taiji.wallet.utils.ExchangeCalculator;
import io.taiji.wallet.views.DontShowNegativeFormatter;
import io.taiji.wallet.views.HourXFormatter;
import io.taiji.wallet.views.WeekXFormatter;
import io.taiji.wallet.views.YearXFormatter;


public class FragmentApplications extends Fragment {

    private TextView price, chartTitle;
    private SwipeRefreshLayout swipeLayout;
    private MainActivity ac;

    private static String[] TITLE_TEXTS;
    private int displayType = 1;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_applications, container, false);
        ac = (MainActivity) getActivity();
        price = (TextView) rootView.findViewById(R.id.price);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout2);
        swipeLayout.setColorSchemeColors(ac.getResources().getColor(R.color.colorPrimary));
        general();
        return rootView;
    }

    // update application list
    public void update(boolean updateChart) {

    }

    private void general() {
        if (ac != null && ac.getPreferences() != null) {
            SharedPreferences.Editor editor = ac.getPreferences().edit();
            editor.putInt("displaytype_chart", displayType);
            editor.apply();
        }
    }
}