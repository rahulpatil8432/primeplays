package com.rkonline.android;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class adapter_market extends RecyclerView.Adapter<adapter_market.ViewHolder> {

    Context context;
    ArrayList<String> names;
    ArrayList<Boolean> isOpen;
    ArrayList<String> openTimeArray;
    ArrayList<String> closeTimeArray;
    ArrayList<Integer> marketStatus;

    public adapter_market(Context context, String game,
                          ArrayList<String> names,
                          ArrayList<Boolean> isOpen,
                          ArrayList<String> openTimeArray,
                          ArrayList<String> closeTimeArray,
                          ArrayList<Integer> marketStatus) {

        this.context = context;
        this.names = names;
        this.isOpen = isOpen;
        this.openTimeArray = openTimeArray;
        this.closeTimeArray = closeTimeArray;
        this.marketStatus = marketStatus;
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
        switch (status) {
            case 1:
                bgColor = ContextCompat.getColor(context, R.color.green);
                break;
            case 2:
                bgColor = ContextCompat.getColor(context, R.color.md_blue_100);
                break;
            default:
                bgColor = ContextCompat.getColor(context, R.color.md_blue_grey_500);
                break;
        }
        holder.layout.setBackgroundColor(bgColor);

        holder.layout.setOnClickListener(v -> {

            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            if (marketStatus.get(pos) == 1) { // OPEN

                Intent go = new Intent(context, rate.class);
                go.putExtra("header", "Select Game");
                go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(go);

            } else if (marketStatus.get(pos) == 2) {

                Toast.makeText(context,
                        "Market not opened yet",
                        Toast.LENGTH_SHORT).show();

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


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, openTime,closeTime;
        RelativeLayout layout;

        public ViewHolder(@NonNull View view) {
            super(view);
            name = view.findViewById(R.id.name);
            openTime = view.findViewById(R.id.openTime);
            closeTime = view.findViewById(R.id.closeTime);
            layout = view.findViewById(R.id.layout);
        }
    }
}
