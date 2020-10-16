package io.taiji.wallet.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import io.taiji.wallet.R;
import io.taiji.wallet.activities.MainActivity;


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