package com.rkonline.android.adapter;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rkonline.android.R;

import java.util.ArrayList;

public class SelectedNumberAdapter extends RecyclerView.Adapter<SelectedNumberAdapter.ViewHolder> {

    private ArrayList<String> selectedNumbers;
    private ArrayList<String> amounts;
    private OnAmountChangedListener listener;

    public interface OnAmountChangedListener {
        void onAmountChanged(int total);
        void onDelete(int position);
    }

    public SelectedNumberAdapter(ArrayList<String> selectedNumbers, ArrayList<String> amounts, OnAmountChangedListener listener) {
        this.selectedNumbers = selectedNumbers;
        this.amounts = amounts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_selected_number, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.numberText.setText(selectedNumbers.get(position));
        if (holder.textWatcher   != null) {
            holder.amountEdit.removeTextChangedListener(holder.textWatcher);
        }
        holder.amountEdit.setText(amounts.get(position));
        holder.textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos == RecyclerView.NO_POSITION) return;
                String value = s.toString().trim();

                if (TextUtils.isEmpty(value)) {
                    amounts.set(adapterPos, "");
                    listener.onAmountChanged(calculateTotal());
                    return;
                }

                amounts.set(adapterPos, value);

                listener.onAmountChanged(calculateTotal());
            }
        };

        holder.amountEdit.addTextChangedListener(holder.textWatcher);

        holder.deleteBtn.setOnClickListener(v -> {
            listener.onDelete(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return selectedNumbers.size();
    }
    private int calculateTotal() {
        int total = 0;
        for (String amt : amounts) {
            if (!TextUtils.isEmpty(amt)) {
                try {
                    total += Integer.parseInt(amt);
                } catch (NumberFormatException ignored) {}
            }
        }
        return total;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView numberText;
        EditText amountEdit;
        ImageButton deleteBtn;
        TextWatcher textWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            numberText = itemView.findViewById(R.id.numberText);
            amountEdit = itemView.findViewById(R.id.amountEdit);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}
