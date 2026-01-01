package com.rkonline.android.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rkonline.android.R;

import java.util.ArrayList;
import java.util.Map;

public class SelectedNumberAdapter
        extends RecyclerView.Adapter<SelectedNumberAdapter.ViewHolder> {

    private final ArrayList<String> numbers;
    private final Map<String, String> amountMap;
    private final OnAmountChangedListener listener;

    public interface OnAmountChangedListener {
        void onAmountChanged();
    }

    public SelectedNumberAdapter(
            ArrayList<String> numbers,
            Map<String, String> amountMap,
            OnAmountChangedListener listener
    ) {
        this.numbers = numbers;
        this.amountMap = amountMap;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_selected_number, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String number = numbers.get(position);
        holder.numberText.setText(number);

        if (holder.textWatcher != null)
            holder.amountEdit.removeTextChangedListener(holder.textWatcher);

        holder.amountEdit.setText(amountMap.get(number));

        holder.textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                amountMap.put(number, s.toString().trim());
                listener.onAmountChanged();
            }
        };

        holder.amountEdit.addTextChangedListener(holder.textWatcher);
    }

    @Override
    public int getItemCount() {
        return numbers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView numberText;
        EditText amountEdit;
        TextWatcher textWatcher;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            numberText = itemView.findViewById(R.id.numberText);
            amountEdit = itemView.findViewById(R.id.amountEdit);
        }
    }
}
