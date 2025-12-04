package com.rkonline.android;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class adapter_crossing extends RecyclerView.Adapter<adapter_crossing.ViewHolder> {

    Context context;

    ArrayList<String> number = new ArrayList<>();
    private ArrayList<String> list = new ArrayList<>();

    public adapter_crossing(Context context, ArrayList<String> number) {
        this.context = context;
        this.number = number;
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

        holder.number.setVisibility(View.GONE);

        holder.amount.setFocusable(false);
        holder.amount.setEnabled(false);
        holder.amount.setClickable(false);
        holder.amount.setText(number.get(position));

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
