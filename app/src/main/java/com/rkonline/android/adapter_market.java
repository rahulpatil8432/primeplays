package com.rkonline.android;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class adapter_market extends RecyclerView.Adapter<adapter_market.ViewHolder> {

    public static final int MARKET_OPEN = 1;
    public static final int MARKET_YET_TO_OPEN = 2;

    Context context;
    ArrayList<String> names;
    ArrayList<String> openTimeArray;
    ArrayList<String> closeTimeArray;
    ArrayList<Integer> marketStatus;
    ArrayList<String> marketResults;

    public adapter_market(Context context,
                          ArrayList<String> names,
                          ArrayList<String> openTimeArray,
                          ArrayList<String> closeTimeArray,
                          ArrayList<Integer> marketStatus,
                          ArrayList<String> marketResults) {

        this.context = context;
        this.names = names;
        this.openTimeArray = openTimeArray;
        this.closeTimeArray = closeTimeArray;
        this.marketStatus = marketStatus;
        this.marketResults = marketResults;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.market_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.name.setText(names.get(position));
        holder.openTime.setText(openTimeArray.get(position));
        holder.closeTime.setText(closeTimeArray.get(position));

        int status = marketStatus.get(position);
        int bgColor;
        int headerColor;
        switch (status) {
            case MARKET_OPEN:
                headerColor =  ContextCompat.getColor(context, R.color.md_green_800);
                bgColor = ContextCompat.getColor(context, R.color.md_green_600);
                holder.layout.setAlpha(0.9f);
                break;

            case MARKET_YET_TO_OPEN:
                bgColor = ContextCompat.getColor(context, R.color.md_blue_400);
                holder.layout.setAlpha(0.9f);
                headerColor = ContextCompat.getColor(context, R.color.md_blue_800);
                break;

            default:
                bgColor = ContextCompat.getColor(context, R.color.md_grey_800);
                headerColor = ContextCompat.getColor(context, R.color.md_grey_900);
                holder.layout.setAlpha(0.5f);
                break;
        }
        holder.marketResult.setText(marketResults.get(position));
        holder.layout.setBackgroundColor(bgColor);
        holder.name.setBackgroundColor(headerColor);

        holder.layout.setOnClickListener(v -> {

            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            if (marketStatus.get(pos) == MARKET_OPEN) {

                Intent go = new Intent(context, rate.class);
                go.putExtra("header", "Select Game");
                go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(go);

            } else if (marketStatus.get(pos) == MARKET_YET_TO_OPEN) {

                Toast.makeText(context,
                        "Market not open yet. Betting is open.",
                        Toast.LENGTH_SHORT).show();

                Intent go = new Intent(context, rate.class);
                go.putExtra("header", "Select Game");
                go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(go);

            } else {

                Toast.makeText(context,
                        "Market is closed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, openTime, closeTime, marketResult;
        RelativeLayout layout;

        public ViewHolder(@NonNull View view) {
            super(view);
            name = view.findViewById(R.id.name);
            openTime = view.findViewById(R.id.openTime);
            closeTime = view.findViewById(R.id.closeTime);
            marketResult = view.findViewById(R.id.marketResult);
            layout = view.findViewById(R.id.layout);
        }
    }
}
