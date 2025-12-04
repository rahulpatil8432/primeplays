package com.rkonline.android;

public class ChartModel {
    public String date;
    public String openResult;
    public String closeResult;

    public ChartModel() {}

    public ChartModel(String date, String openResult, String closeResult) {
        this.date = date;
        this.openResult = openResult;
        this.closeResult = closeResult;
    }
}