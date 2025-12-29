package com.rkonline.android.timetable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.rkonline.android.R;
import com.rkonline.android.model.MarketModel;

import java.util.ArrayList;

public class TimeTableAdapter extends RecyclerView.Adapter<TimeTableAdapter.VH> {

    ArrayList<MarketModel> list;

    public TimeTableAdapter(ArrayList<MarketModel> list) {
        this.list = list;
    }

    class VH extends RecyclerView.ViewHolder {
        TextView name, open, close;

        VH(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            open = v.findViewById(R.id.open);
            close = v.findViewById(R.id.close);
        }
    }

    @Override
    public VH onCreateViewHolder(ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext())
                .inflate(R.layout.row_time_table, p, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(VH h, int i) {
        MarketModel m = list.get(i);
        h.name.setText(m.name);
        h.open.setText(m.open);
        h.close.setText(m.close);
    }

    @Override
    public int getItemCount() { return list.size(); }
}

