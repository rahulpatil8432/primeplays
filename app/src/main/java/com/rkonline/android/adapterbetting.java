package com.rkonline.android;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class adapterbetting extends RecyclerView.Adapter<adapterbetting.ViewHolder> {

    Context context;

    ArrayList<String> number = new ArrayList<>();
    private ArrayList<String> list = new ArrayList<>();
    public interface AmountChangeListener {
        void onAmountChanged(ArrayList<String> updatedList);
    }
    private AmountChangeListener listener;

    public adapterbetting(Context context,ArrayList<String> number,AmountChangeListener listener) {
        this.context = context;
        this.number = number;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.betlayout, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        list.add("0");

        holder.number.setText(number.get(position));

        holder.amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String text = (s != null) ? s.toString() : "";
                if (text.isEmpty()) {
                    list.set(position, "0");
                } else {
                    try {
                        int value = Integer.parseInt(text);
                        if (value > 10000) {
                            holder.amount.setText("10000");
                            list.set(position, "10000");
                        } else {
                            list.set(position, text);
                        }
                    } catch (NumberFormatException e) {
                        list.set(position, "0");
                    }
                }
                if (listener != null)
                    listener.onAmountChanged(list);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        holder.setIsRecyclable(false);
    }

    public ArrayList<String> getNumber()
    {
        return list;
    }

    @Override
    public int getItemCount() {
        return number.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView number;
        EditText amount;

        public ViewHolder(View view) {
            super(view);
            number = view.findViewById(R.id.number);
            amount = view.findViewById(R.id.amount);


        }
    }



}
