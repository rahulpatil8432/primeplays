package com.rkonline.android.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.rkonline.android.R;
import com.rkonline.android.model.WithdrawRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WithdrawAdapter extends RecyclerView.Adapter<WithdrawAdapter.WithdrawViewHolder> {

    private List<WithdrawRequest> requests;

    public WithdrawAdapter(List<WithdrawRequest> requests) {
        this.requests = requests;
    }

    @NonNull
    @Override
    public WithdrawViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_withdraw_request, parent, false);
        return new WithdrawViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WithdrawViewHolder holder, int position) {
        WithdrawRequest request = requests.get(position);
        holder.remarkContainer.setVisibility(View.GONE);
        holder.amount.setText("Amount: ₹" + request.getAmount());
        String status = request.getStatus().toLowerCase();

        holder.status.setText(status.substring(0,1).toUpperCase() + status.substring(1));

        if (status.equals("pending")) {
            holder.status.setBackgroundResource(R.drawable.bg_status_pending);
            holder.statusStrip.setBackgroundColor(Color.parseColor("#FF9800"));
        }
        else if (status.equals("approved")) {
            holder.status.setBackgroundResource(R.drawable.bg_status_approved);
            holder.statusStrip.setBackgroundColor(Color.parseColor("#1B5E20"));
        }
        else if (status.equals("rejected")) {
            holder.status.setBackgroundResource(R.drawable.bg_status_rejected);
            holder.statusStrip.setBackgroundColor(Color.parseColor("#B71C1C"));
        }
        if (TextUtils.isEmpty(request.getUpi()) || request.getUpi().trim().isEmpty()) {
            holder.upi.setText("UPI: - ");
            holder.upi.setVisibility(View.GONE);
        }else{
            holder.upi.setVisibility(View.VISIBLE);
            holder.upi.setText("UPI: " + request.getUpi());
        }
        if (TextUtils.isEmpty(request.getAccountNo()) || request.getAccountNo().trim().isEmpty()) {
            holder.accountNo.setText("Account: -" );
            holder.IFSCCode.setText("IFSC: -" );
            holder.accountNo.setVisibility(View.GONE);
            holder.IFSCCode.setVisibility(View.GONE);
        }else{
            holder.accountNo.setVisibility(View.VISIBLE);
            holder.IFSCCode.setVisibility(View.VISIBLE);
            holder.accountNo.setText("Account: " + request.getAccountNo());
            holder.IFSCCode.setText("IFSC: " + request.getIFSCCode());
        }
        if (!TextUtils.isEmpty(request.getRemark())) {
            holder.remark.setText(request.getRemark());
            holder.remarkContainer.setVisibility(View.VISIBLE);
        }

        holder.mobile.setText("Mobile: " + request.getMobile());

        Date date = new Date(request.getTimestamp());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        holder.timestamp.setText(sdf.format(date));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class WithdrawViewHolder extends RecyclerView.ViewHolder {
        TextView amount, status, upi, accountNo, IFSCCode, mobile, timestamp;
        View statusStrip;
        CardView cardRoot;
        LinearLayout remarkContainer;
        TextView remark;


        public WithdrawViewHolder(@NonNull View itemView) {
            super(itemView);
            amount = itemView.findViewById(R.id.amount);
            status = itemView.findViewById(R.id.status);
            upi = itemView.findViewById(R.id.upi);
            accountNo = itemView.findViewById(R.id.accountNo);
            IFSCCode = itemView.findViewById(R.id.IFSCCode);
            mobile = itemView.findViewById(R.id.mobile);
            timestamp = itemView.findViewById(R.id.timestamp);
            statusStrip = itemView.findViewById(R.id.statusStrip);
            cardRoot = itemView.findViewById(R.id.cardRoot);
            remarkContainer = itemView.findViewById(R.id.remarkContainer);
            remark = itemView.findViewById(R.id.remark);
        }
    }
}
