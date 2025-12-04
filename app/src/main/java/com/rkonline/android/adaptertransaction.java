package com.rkonline.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class adaptertransaction extends RecyclerView.Adapter<adaptertransaction.ViewHolder> {

    Context context;

    ArrayList<String> date;
    ArrayList<String> remark;
    ArrayList<String> amount;

    public adaptertransaction(Context context,
                              ArrayList<String> date,
                              ArrayList<String> remark,
                              ArrayList<String> amount) {

        this.context = context;
        this.date = date;
        this.remark = remark;
        this.amount = amount;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_transactions_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        h.date.setText(date.get(pos));
        h.amount.setText(amount.get(pos));
        h.remark.setText(remark.get(pos));
    }

    @Override
    public int getItemCount() {
        return date.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView date, amount, remark;

        public ViewHolder(@NonNull View v) {
            super(v);

            date = v.findViewById(R.id.row_date);
            amount = v.findViewById(R.id.row_amount);
            remark = v.findViewById(R.id.row_narration);
        }
    }
}
