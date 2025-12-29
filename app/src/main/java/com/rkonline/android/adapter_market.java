package com.rkonline.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
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

import com.rkonline.android.utils.AlertHelper;

import java.util.ArrayList;

class adapter_market extends RecyclerView.Adapter<adapter_market.ViewHolder> {

    public static final int MARKET_OPEN = 1;
    public static final int MARKET_YET_TO_OPEN = 2;
    public static final int MARKET_CLOSE_TODAY = 3;

    Context context;
    ArrayList<String> names;
    ArrayList<String> openTimeArray;
    ArrayList<String> closeTimeArray;
    ArrayList<Integer> marketStatus;
    ArrayList<String> marketResults;
    ArrayList<Boolean> closeNextDayArray;

    public adapter_market(Context context,
                          ArrayList<String> names,
                          ArrayList<String> openTimeArray,
                          ArrayList<String> closeTimeArray,
                          ArrayList<Integer> marketStatus,
                          ArrayList<String> marketResults,
                          ArrayList<Boolean> closeNextDayArray) {

        this.context = context;
        this.names = names;
        this.openTimeArray = openTimeArray;
        this.closeTimeArray = closeTimeArray;
        this.marketStatus = marketStatus;
        this.marketResults = marketResults;
        this.closeNextDayArray = closeNextDayArray;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.market_layout, parent, false);
        return new ViewHolder(v);
    }

    private static class MarketStatusInfo {
        final int bgColor;
        final int iconRes;
        final int iconColor;
        final float alpha;
        final String statusText;
        final boolean canOpenMarket;
        final String toastMessage;

        MarketStatusInfo(int bgColor, int iconRes, int iconColor, float alpha, String statusText,
                         boolean canOpenMarket, String toastMessage) {
            this.bgColor = bgColor;
            this.iconRes = iconRes;
            this.iconColor = iconColor;
            this.alpha = alpha;
            this.statusText = statusText;
            this.canOpenMarket = canOpenMarket;
            this.toastMessage = toastMessage;
        }
    }

    private MarketStatusInfo getMarketStatusInfo(int status) {
        switch (status) {
            case MARKET_OPEN:
                return new MarketStatusInfo(
                        ContextCompat.getColor(context, R.color.md_green_900),
                        R.drawable.play_icon_circle,
                        ContextCompat.getColor(context, R.color.md_green_900),
                        0.9f,
                        "Close is running",
                        true,
                        null
                );

            case MARKET_YET_TO_OPEN:
                return new MarketStatusInfo(
                        ContextCompat.getColor(context, R.color.md_green_900),
                        R.drawable.play_icon_circle,
                        ContextCompat.getColor(context, R.color.md_green_900),
                        0.9f,
                        "Bet is running",
                        false,
                        "Market not open yet. Betting is open."
                );

            case MARKET_CLOSE_TODAY:
                return new MarketStatusInfo(
                        ContextCompat.getColor(context, R.color.md_red_900),
                        R.drawable.close_icon,
                        ContextCompat.getColor(context, R.color.md_red_900),
                        0.55f,
                        "Close for today",
                        false,
                        "Today is the weekly off for this market. Betting will start tomorrow."
                );

            default:
                return new MarketStatusInfo(
                        ContextCompat.getColor(context, R.color.md_red_900),
                        R.drawable.close_icon,
                        ContextCompat.getColor(context, R.color.md_red_900),
                        0.55f,
                        "Close for today",
                        false,
                        "Betting is closed for today.\n Please come next day to play"
                );
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(names.get(position));
        holder.openTime.setText(openTimeArray.get(position));
        holder.closeTime.setText(closeTimeArray.get(position));
        holder.marketResult.setText(marketResults.get(position));

        MarketStatusInfo statusInfo = getMarketStatusInfo(marketStatus.get(position));

        holder.layout.setAlpha(statusInfo.alpha);
        holder.statusText.setText(statusInfo.statusText);
        holder.statusText.setTextColor(statusInfo.bgColor);
        holder.icon.setImageResource(statusInfo.iconRes);
        holder.icon.setColorFilter(statusInfo.iconColor, PorterDuff.Mode.SRC_IN);

        holder.layout.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            MarketStatusInfo info = getMarketStatusInfo(marketStatus.get(pos));

            if (info.canOpenMarket) {
                Intent go = new Intent(context, rate.class);
                go.putExtra("header", "Select Game");
                go.putExtra("market", names.get(pos));
                go.putExtra("isMarketOpen", true);
                go.putExtra("openTime", openTimeArray.get(pos));
                go.putExtra("closeTime", closeTimeArray.get(pos));
                go.putExtra("closeNextDay", closeNextDayArray.get(pos));
                go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(go);

            } else {
                if (marketStatus.get(pos) == MARKET_YET_TO_OPEN) {
                    Intent go = new Intent(context, rate.class);
                    go.putExtra("header", "Select Game");
                    go.putExtra("market", names.get(pos));
                    go.putExtra("isMarketOpen", false);
                    go.putExtra("openTime", openTimeArray.get(pos));
                    go.putExtra("closeTime", closeTimeArray.get(pos));
                    go.putExtra("closeNextDay", closeNextDayArray.get(pos));
                    go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(go);
                } else {
                    AlertHelper.showCustomAlert(
                            context,
                            "Sorry!",
                            info.toastMessage,
                            R.drawable.close_icon,
                            R.color.md_red_900
                    );
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, openTime, closeTime, marketResult,statusText;
        RelativeLayout layout;
        ImageView icon;

        public ViewHolder(@NonNull View view) {
            super(view);
            name = view.findViewById(R.id.name);
            openTime = view.findViewById(R.id.openTime);
            closeTime = view.findViewById(R.id.closeTime);
            statusText = view.findViewById(R.id.statusText);
            marketResult = view.findViewById(R.id.marketResult);
            layout = view.findViewById(R.id.layout);
            icon = view.findViewById(R.id.icon);
        }
    }
}
