package io.taiji.wallet.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.taiji.wallet.R;
import io.taiji.wallet.data.TransactionDisplay;


public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<TransactionDisplay> boxlist;
    private int lastPosition = -1;
    private SimpleDateFormat dateformat = new SimpleDateFormat("dd. MMMM yyyy, HH:mm", Locale.getDefault());
    private View.OnCreateContextMenuListener contextMenuListener;
    private View.OnClickListener clickListener;
    private int position;

    private static final int CONTENT = 0;

    @Override
    public int getItemViewType(int position) {
        return CONTENT;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView month, walletbalance, walletname, other_address, plusminus;
        ImageView my_addressicon, other_addressicon, type;
        private LinearLayout container;

        MyViewHolder(View view) {
            super(view);
            month = (TextView) view.findViewById(R.id.month);
            walletbalance = (TextView) view.findViewById(R.id.walletbalance);
            plusminus = (TextView) view.findViewById(R.id.plusminus);
            walletname = (TextView) view.findViewById(R.id.walletname);
            other_address = (TextView) view.findViewById(R.id.other_address);

            my_addressicon = (ImageView) view.findViewById(R.id.my_addressicon);
            other_addressicon = (ImageView) view.findViewById(R.id.other_addressicon);
            type = (ImageView) view.findViewById(R.id.type);
            container = (LinearLayout) view.findViewById(R.id.container);
        }

        void clearAnimation() {
            container.clearAnimation();
        }
    }


    public TransactionAdapter(List<TransactionDisplay> boxlist, Context context, View.OnClickListener clickListener, View.OnCreateContextMenuListener listener) {
        Log.i("TAG", "boxlist = " +boxlist.size());
        this.boxlist = boxlist;
        this.context = context;
        this.contextMenuListener = listener;
        this.clickListener = clickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_w_transaction, parent, false);
        itemView.setOnCreateContextMenuListener(contextMenuListener);
        itemView.setOnClickListener(clickListener);
        return new MyViewHolder(itemView);
    }

    public static int calculateBoxPosition(int position){
        return position;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder_, final int position) {
        if (getItemViewType(position) == CONTENT) {
            MyViewHolder holder = (MyViewHolder) holder_;
            TransactionDisplay box = boxlist.get(calculateBoxPosition(position));
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    setPosition(position);
                    return false;
                }
            });

            holder.walletbalance.setText(UnitCalculator.getInstance().displayBalanceNicely(UnitCalculator.getInstance().convertUnit(Math.abs(box.getAmount()), UnitCalculator.getInstance().getCurrent().getUnit())) + " " + UnitCalculator.getInstance().getCurrencyShort());
            String walletname = AddressNameConverter.getInstance(context).get(box.getFromAddress());
            holder.walletname.setText(walletname == null ? box.getWalletName() : walletname);

            String toName = AddressNameConverter.getInstance(context).get(box.getToAddress());
            holder.other_address.setText(toName == null ? box.getToAddress() : toName + " (" + box.getToAddress().substring(0, 10) + ")");
            holder.plusminus.setText(box.getType());
            holder.plusminus.setTextColor(context.getResources().getColor(box.getType().equals("+") ? R.color.taijiReceived : R.color.taijiSpent));
            holder.walletbalance.setTextColor(context.getResources().getColor(box.getType().equals("+") ? R.color.taijiReceived : R.color.taijiSpent));
            holder.container.setAlpha(1f);
            holder.type.setVisibility(View.INVISIBLE);
            holder.my_addressicon.setImageBitmap(Blockies.createIcon(box.getFromAddress()));
            holder.other_addressicon.setImageBitmap(Blockies.createIcon(box.getToAddress()));

            setAnimation(holder.container, position);
        }
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if(holder instanceof MyViewHolder)
            holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_bottom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }


    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if(holder instanceof MyViewHolder)
            ((MyViewHolder)holder).clearAnimation();
    }

    @Override
    public int getItemCount() {
        return boxlist.size();
    }
}