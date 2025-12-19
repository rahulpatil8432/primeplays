package com.rkonline.android;

public class ChartModel {
    public String date;
    public String aankdoOpen;
    public String aankdoClose;
    public String jodi;

    public ChartModel() {}

    public ChartModel(String date, String aankdoOpen, String aankdoClose, String jodi) {
        this.date = date;
        this.aankdoOpen = aankdoOpen;
        this.aankdoClose = aankdoClose;
        this.jodi = jodi;
    }
}
