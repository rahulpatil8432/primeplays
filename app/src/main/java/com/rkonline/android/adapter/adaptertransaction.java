package com.rkonline.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rkonline.android.R;
import com.rkonline.android.model.TransactionModel;

import java.util.ArrayList;
public class adaptertransaction extends RecyclerView.Adapter<adaptertransaction.ViewHolder> {

    Context context;
    ArrayList<TransactionModel> list;

    public adaptertransaction(Context context, ArrayList<TransactionModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_transactions_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        TransactionModel m = list.get(pos);

        h.date.setText(m.date);
        h.remark.setText(m.remark);
        h.balance.setText("Balance: ₹" + m.balance);

        if (m.type.equalsIgnoreCase("CREDIT")) {
            h.amount.setText("+ ₹" + m.amount);
            h.amount.setTextColor(context.getColor(R.color.md_green_900));
            h.type.setText("CREDIT");
            h.type.setTextColor(context.getColor(R.color.md_green_900));
            h.statusStrip.setBackgroundColor(context.getColor(R.color.md_green_900));
        } else {
            h.amount.setText("- ₹" + m.amount);
            h.amount.setTextColor(context.getColor(R.color.md_red_900));
            h.type.setText("DEBIT");
            h.type.setTextColor(context.getColor(R.color.md_red_900));
            h.statusStrip.setBackgroundColor(context.getColor(R.color.md_red_900));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView date, amount, remark, balance, type;
        View statusStrip;

        public ViewHolder(@NonNull View v) {
            super(v);
            date = v.findViewById(R.id.txt_date);
            amount = v.findViewById(R.id.txt_amount);
            remark = v.findViewById(R.id.txt_remark);
            balance = v.findViewById(R.id.txt_balance);
            type = v.findViewById(R.id.txt_type);
            statusStrip = v.findViewById(R.id.statusStrip);
        }
    }
}
