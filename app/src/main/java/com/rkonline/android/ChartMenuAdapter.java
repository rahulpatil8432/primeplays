package com.rkonline.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChartMenuAdapter extends RecyclerView.Adapter<ChartMenuAdapter.ViewHolder> {

    Context context;
    List<MarketModel> list;
    OnMarketClick listener;

    public interface OnMarketClick {
        void onClick(MarketModel m);
    }

    public ChartMenuAdapter(Context context, List<MarketModel> list, OnMarketClick listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_chart_menu_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        MarketModel m = list.get(pos);
        holder.name.setText(m.name);
        holder.result.setText(m.openResult + " - " + m.closeResult);

        holder.itemView.setOnClickListener(v -> listener.onClick(m));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, result;

        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.marketTitle);
            result = v.findViewById(R.id.marketResult);
        }
    }
}
