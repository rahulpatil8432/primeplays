package com.rkonline.android.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rkonline.android.R;
import com.rkonline.android.betting;
import com.rkonline.android.crossing;
import com.rkonline.android.fullsangam;
import com.rkonline.android.halfsangam;
import com.rkonline.android.model.GameHandler;
import com.rkonline.android.utils.AlertHelper;
import com.rkonline.android.utils.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class adapter_game extends RecyclerView.Adapter<adapter_game.ViewHolder> {

    private final Context context;
    private final ArrayList<String> gameNames;
    private final String market, openTime, closeTime;
    private final boolean isMarketOpen, closeNextDay;

    private final Map<String, GameHandler> GameHandlerMap = new HashMap<>();

    public adapter_game(Context context, ArrayList<String> gameNames,
                       String market, boolean isMarketOpen, String openTime, String closeTime, boolean closeNextDay) {
        this.context = context;
        this.gameNames = gameNames;
        this.market = market;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isMarketOpen = isMarketOpen;
        this.closeNextDay = closeNextDay;

        GameHandlerMap.put("Single Ank", new GameHandler(R.drawable.ic_single_digit,GameData::getSingleAnk, betting.class));
        GameHandlerMap.put("Jodi", new GameHandler(R.drawable.ic_jodi,GameData::getJodi, betting.class));
        GameHandlerMap.put("Crossing", new GameHandler(R.drawable.ic_jodi,GameData::getJodi, crossing.class));
        GameHandlerMap.put("Red Jodi", new GameHandler(R.drawable.ic_jodi,GameData::getRedJodi, betting.class));
        GameHandlerMap.put("Single Pana", new GameHandler(R.drawable.ic_single_pana,GameData::getSinglePana, betting.class));
        GameHandlerMap.put("SP Motor", new GameHandler(R.drawable.ic_jodi,GameData::getSinglePana, crossing.class));
        GameHandlerMap.put("Double Pana", new GameHandler(R.drawable.ic_double_pana,GameData::getDoublePana, betting.class));
        GameHandlerMap.put("Triple Pana", new GameHandler(R.drawable.ic_triple_pana,GameData::getTriplePana, betting.class));
        GameHandlerMap.put("Half Sangam", new GameHandler(R.drawable.ic_half_sangam,GameData::getTriplePana, halfsangam.class));
        GameHandlerMap.put("Full Sangam", new GameHandler(R.drawable.ic_full_sangam,GameData::getTriplePana, fullsangam.class));
        GameHandlerMap.put("DP Motor", new GameHandler(R.drawable.ic_jodi,GameData::getDoublePana, crossing.class));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final String gameName = gameNames.get(position);
        holder.name.setText(gameName);
        GameHandler handler = GameHandlerMap.get(gameName.trim());

        if (handler != null) {
            holder.gameIcon.setImageResource(handler.icon);
            holder.layout.setOnClickListener(v -> {
                ArrayList<String> numbers = handler.generateNumbers();
                Intent intent = new Intent(context, handler.targetActivity);
                intent.putExtra("list", numbers);
                intent.putExtra("game", gameName);
                intent.putExtra("market", market);
                intent.putExtra("openTime", openTime);
                intent.putExtra("closeTime", closeTime);
                intent.putExtra("isMarketOpen", isMarketOpen);
                intent.putExtra("closeNextDay", closeNextDay);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (!market.isEmpty()) context.startActivity(intent);
            });
        } else {
            AlertHelper.showCustomAlert(context, "Error" , gameName+ " Have some problem", 0,0);
        }
    }

    @Override
    public int getItemCount() {
        return gameNames.size();
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
}
