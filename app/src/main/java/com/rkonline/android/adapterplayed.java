package com.rkonline.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class adapterplayed extends RecyclerView.Adapter<adapterplayed.ViewHolder> {

    private final Context context;

    private final ArrayList<String> date;
    private final ArrayList<String> bazar;
    private final ArrayList<String> amount;
    private final ArrayList<String> bet;

    public adapterplayed(Context context,
                         ArrayList<String> date,
                         ArrayList<String> bazar,
                         ArrayList<String> amount,
                         ArrayList<String> bet) {

        this.context = context;
        this.date = date != null ? date : new ArrayList<>();
        this.bazar = bazar != null ? bazar : new ArrayList<>();
        this.amount = amount != null ? amount : new ArrayList<>();
        this.bet = bet != null ? bet : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.played, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.date.setText(date.get(position));
        holder.bazar.setText(bazar.get(position));
        holder.amount.setText(amount.get(position) + " 📀");
        holder.bet.setText(bet.get(position));
    }

    @Override
    public int getItemCount() {
        return date.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView date, bazar, amount, bet;

        public ViewHolder(View view) {
            super(view);

            date = view.findViewById(R.id.date);
            bazar = view.findViewById(R.id.bazar);
            amount = view.findViewById(R.id.amount);
            bet = view.findViewById(R.id.bet);
        }
    }
}