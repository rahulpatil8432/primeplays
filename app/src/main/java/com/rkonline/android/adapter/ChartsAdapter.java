package com.rkonline.android.adapter;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.rkonline.android.R;

import java.util.List;

public class ChartsAdapter extends RecyclerView.Adapter<ChartsAdapter.ChartViewHolder> {

    private final List<DocumentSnapshot> chartList;

    public ChartsAdapter(List<DocumentSnapshot> chartList) {
        this.chartList = chartList;
    }

    @NonNull
    @Override
    public ChartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chart, parent, false);
        return new ChartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChartViewHolder holder, int position) {

        DocumentSnapshot doc = chartList.get(position);

        String date = doc.getString("date");
        String open = doc.getString("aankdo_open");
        String jodi = doc.getString("jodi");
        String close = doc.getString("aankdo_close");

        holder.txtDate.setText(date != null ? date : "");

        // If result incomplete
        if (open == null || jodi == null || close == null ||
                open.isEmpty() || jodi.isEmpty() || close.isEmpty()) {

            holder.txtResult.setText("---");
            holder.txtResult.setTextColor(Color.GRAY);
            return;
        }

        String result = open + " - " + jodi + " - " + close;
        SpannableString spannable = new SpannableString(result);

        // OPEN (Green)
        spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor("#2E7D32")),
                0,
                open.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // JODI (Blue)
        int jodiStart = open.length() + 3;
        spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor("#1565C0")),
                jodiStart,
                jodiStart + jodi.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // CLOSE (Red)
        int closeStart = jodiStart + jodi.length() + 3;
        spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor("#C62828")),
                closeStart,
                result.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        holder.txtResult.setText(spannable);
    }

    @Override
    public int getItemCount() {
        return chartList == null ? 0 : chartList.size();
    }

    static class ChartViewHolder extends RecyclerView.ViewHolder {

        TextView txtDate;
        TextView txtResult;

        public ChartViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtResult = itemView.findViewById(R.id.txtResult);
        }
    }
}
