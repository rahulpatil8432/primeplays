package com.rkonline.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChartsAdapter extends RecyclerView.Adapter<ChartsAdapter.VH> {

    Context context;
    List<ChartModel> list;

    public ChartsAdapter(Context context, List<ChartModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_chart, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ChartModel m = list.get(pos);

        h.date.setText(m.date);
        h.open.setText(m.aankdoOpen != null ? m.aankdoOpen : "-");
        h.close.setText(m.aankdoClose != null ? m.aankdoClose : "-");
        h.jodi.setText(m.jodi != null ? m.jodi : "-");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView date, open, close, jodi;

        VH(View v) {
            super(v);
            date = v.findViewById(R.id.txtDate);
            open = v.findViewById(R.id.txtOpen);
            close = v.findViewById(R.id.txtClose);
            jodi = v.findViewById(R.id.txtJodi);
        }
    }
}

