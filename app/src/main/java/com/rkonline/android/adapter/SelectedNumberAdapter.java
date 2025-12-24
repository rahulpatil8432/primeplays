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
        holder.amountEdit.setText(amounts.get(position));
        holder.itemView.startAnimation(
                android.view.animation.AnimationUtils
                        .loadAnimation(holder.itemView.getContext(), R.animator.row_enter)
        );
        holder.amountEdit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString().trim();
                if (TextUtils.isEmpty(value)) {
                    amounts.set(holder.getAdapterPosition(), "0"); // default 0
                } else {
                    amounts.set(holder.getAdapterPosition(), value);
                }

                // Recalculate total
                int total = 0;
                for (String amt : amounts) {
                    try {
                        total += Integer.parseInt(amt);
                    } catch (NumberFormatException e) {
                        total += 0;
                    }
                }
                listener.onAmountChanged(total);
            }
        });

        holder.deleteBtn.setOnClickListener(v -> {
            holder.itemView.animate()
                    .alpha(0f)
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() ->
                            listener.onDelete(holder.getAdapterPosition())
                    )
                    .start();
        });

    }

    @Override
    public int getItemCount() {
        return selectedNumbers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView numberText;
        EditText amountEdit;
        ImageButton deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            numberText = itemView.findViewById(R.id.numberText);
            amountEdit = itemView.findViewById(R.id.amountEdit);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}
