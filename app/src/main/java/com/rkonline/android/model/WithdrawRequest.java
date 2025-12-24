package com.rkonline.android.model;

public class WithdrawRequest {
    private String IFSCCode;
    private String accountNo;
    private int amount;
    private String mobile;
    private String status;
    private long timestamp;
    private String upi;
    private String remark;

    public WithdrawRequest() {}

    public String getIFSCCode() { return IFSCCode; }
    public String getAccountNo() { return accountNo; }
    public int getAmount() { return amount; }
    public String getMobile() { return mobile; }
    public String getStatus() { return status; }
    public long getTimestamp() { return timestamp; }
    public String getUpi() { return upi; }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
