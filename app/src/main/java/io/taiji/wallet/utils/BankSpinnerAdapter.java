package io.taiji.wallet.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BankSpinnerAdapter extends ArrayAdapter<LabelValue> {
    private Context context;
    private LabelValue[] labelValues;

    public BankSpinnerAdapter(Context context, int textViewResourceId,
                            LabelValue[] labelValues) {
        super(context, textViewResourceId, labelValues);
        this.context = context;
        this.labelValues = labelValues;
    }

    public int getCount(){
        return labelValues.length;
    }

    public LabelValue getItem(int position){
        return labelValues[position];
    }

    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = new TextView(context);
        label.setText(labelValues[position].getLabel());
        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = new TextView(context);
        label.setText(labelValues[position].getLabel());
        return label;
    }
}

