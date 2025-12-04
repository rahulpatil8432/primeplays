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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class adapter_market extends RecyclerView.Adapter<adapter_market.ViewHolder> {

    Context context;
//    String game;
    ArrayList<String> names;
    ArrayList<Boolean> isOpen;
    ArrayList<String> openTimeArray;
    ArrayList<String> closeTimeArray;
//    ArrayList<String> numbers;

    public adapter_market(Context context, String game,
                          ArrayList<String> names,
                          ArrayList<Boolean> isOpen,
                          ArrayList<String> openTimeArray,
                          ArrayList<String> closeTimeArray,
                          ArrayList<String> numbers) {

        this.context = context;
//        this.game = game;
        this.names = names;
        this.isOpen = isOpen;
        this.openTimeArray = openTimeArray;
        this.closeTimeArray = closeTimeArray;
//        this.numbers = numbers;
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

        boolean marketIsOpen = isOpen.get(position);

        if (marketIsOpen) {
            holder.layout.setBackgroundColor(
                    context.getResources().getColor(R.color.green)
            );

            holder.layout.setOnClickListener(v -> {

                Intent go;

//                switch (game) {
//                    case "halfsangam":
//                        go = new Intent(context, halfsangam.class);
//                        break;
//
//                    case "fullsangam":
//                        go = new Intent(context, fullsangam.class);
//                        break;
//
//                    case "crossing":
//                        go = new Intent(context, crossing.class);
//                        break;
//
//                    default:
//                        go = new Intent(context, betting.class);
//                        break;
//                }
go =  new Intent(context,rate.class);
                go.putExtra("header", "Select Game");
//                go.putExtra("list", numbers);
//                go.putExtra("game", game);
                go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(go);
            });

        } else {
            holder.layout.setBackgroundColor(
                    context.getResources().getColor(R.color.md_blue_grey_500)
            );

            holder.layout.setOnClickListener(v ->
                    Toast.makeText(context, "Market is closed", Toast.LENGTH_SHORT).show()
            );
        }

        holder.setIsRecyclable(false);
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
