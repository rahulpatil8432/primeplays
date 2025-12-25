package com.rkonline.android.model;

public class TransactionModel {

    public String date;
    public String amount;
    public String remark;
    public String type;   // CREDIT / DEBIT
    public long timestamp;

    public String balance; // calculated

    public TransactionModel() {}

    public TransactionModel(String date, String amount, String remark,
                            String type, long timestamp,String balance) {
        this.date = date;
        this.amount = amount;
        this.remark = remark;
        this.type = type;
        this.timestamp = timestamp;
        this.balance = balance;
    }
}
