package com.rkonline.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChartsAdapter extends RecyclerView.Adapter<ChartsAdapter.ViewHolder> {

    Context context;
    List<ChartModel> list;

    public ChartsAdapter(Context context, List<ChartModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_chart_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ChartModel model = list.get(position);

        holder.date.setText(model.date != null ? model.date : "-");
        holder.open.setText(model.openResult != null ? model.openResult : "-");
        holder.close.setText(model.closeResult != null ? model.closeResult : "-");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView date, open, close;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.chart_date);
            open = itemView.findViewById(R.id.chart_open);
            close = itemView.findViewById(R.id.chart_close);
        }
    }
}
