package com.rkonline.android;

public class GhantaModel {
    public String times, status;

    public GhantaModel(String times, String status) {
        this.times = times;
        this.status = status;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
