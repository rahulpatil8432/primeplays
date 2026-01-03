package com.rkonline.android.adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import android.widget.ImageView;

import com.rkonline.android.R;
import com.rkonline.android.betting;
import com.rkonline.android.crossing;
import com.rkonline.android.fullsangam;
import com.rkonline.android.halfsangam;
import com.rkonline.android.utils.GameData;


public class adapter_game extends RecyclerView.Adapter<adapter_game.ViewHolder> {

    Context context;
    ArrayList<String> name = new ArrayList<>();
    ArrayList<String> rate = new ArrayList<>();
    ArrayList<String> number = new ArrayList<>();
    String market,openTime,closeTime;

    Boolean isMarketOpen, closeNextDay;

    public adapter_game(Context context,  ArrayList<String> name, ArrayList<String> rate, String market, boolean isMarketOpen,String openTime,String closeTime, boolean closeNextDay) {
        this.context = context;
        this.name = name;
        this.rate = rate;
        this.market = market;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isMarketOpen = isMarketOpen;
        this.closeNextDay = closeNextDay;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_layout, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        String gameName = name.get(position);
        holder.name.setText(gameName);
        switch (gameName) {

            case "Single Ank":
                holder.gameIcon.setImageResource(R.drawable.ic_single_digit);
                break;

            case "Single Ank Bulk":
                holder.gameIcon.setImageResource(R.drawable.ic_single_digit_bulk);
                break;

            case "Jodi":
                holder.gameIcon.setImageResource(R.drawable.ic_jodi);
                break;

            case "Jodi Bulk":
                holder.gameIcon.setImageResource(R.drawable.ic_jodi_bulk);
                break;

            case "Single Pana":
                holder.gameIcon.setImageResource(R.drawable.ic_single_pana);
                break;

            case "Double Pana":
                holder.gameIcon.setImageResource(R.drawable.ic_double_pana);
                break;

            case "Triple Pana":
                holder.gameIcon.setImageResource(R.drawable.ic_triple_pana);
                break;

            case "Half Sangam":
                holder.gameIcon.setImageResource(R.drawable.ic_half_sangam);
                break;

            case "Full Sangam":
                holder.gameIcon.setImageResource(R.drawable.ic_full_sangam);
                break;

            default:
                holder.gameIcon.setImageResource(R.drawable.ic_single_digit);
                break;
        }

        /* ---------------- CLICK LOGIC ONLY ---------------- */

        holder.layout.setOnClickListener(v -> {

            number = new ArrayList<>();

            switch (gameName) {
                case "Single Ank":
                    single();
                    break;

                case "Jodi":
                case "Crossing":
                    jodi();
                    break;

                case "Red Jodi":
                    redJodi();
                    break;

                case "Single Pana":
                    singlepatti();
                    break;

                case "Double Pana":
                    doublepatti();
                    break;

                case "Triple Pana":
                    triplepatti();
                    break;

                default:
                    triplepatti();
                    break;
            }

            Intent go;

            switch (gameName) {
                case "Half Sangam":
                    go = new Intent(context, halfsangam.class);
                    break;

                case "Full Sangam":
                    go = new Intent(context, fullsangam.class);
                    break;

                case "Crossing":
                    go = new Intent(context, crossing.class);
                    break;

                default:
                    go = new Intent(context, betting.class);
                    break;
            }

            go.putExtra("list", number);
            go.putExtra("game", gameName);
            go.putExtra("market", market);
            go.putExtra("openTime", openTime);
            go.putExtra("closeTime", closeTime);
            go.putExtra("isMarketOpen", isMarketOpen);
            go.putExtra("closeNextDay", closeNextDay);
            go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(!market.isEmpty()){
                context.startActivity(go);
            }
        });
    }


    @Override
    public int getItemCount() {
        return name.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        LinearLayout layout;
        ImageView gameIcon;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.gameName);
            layout = view.findViewById(R.id.layout);
            gameIcon = view.findViewById(R.id.gameIcon);
        }
    }

    public void single() {
        for (int i = 0; i <= 9; i++) number.add("" + i);
    }
    public void jodi() {
        for (int i = 0; i < 100; i++) {
            String temp = String.format("%02d", i);
            number.add(temp);
        }
    }

    public void redJodi(){
        number.addAll(GameData.getRedJodi());
    }
    public void singlepatti() {
        number.addAll(GameData.getSinglePana());
    }

    public void doublepatti() {
        number.addAll(GameData.getDoublePana());
    }

    public void triplepatti() {
        number.addAll(GameData.getTriplePana());
    }

}
