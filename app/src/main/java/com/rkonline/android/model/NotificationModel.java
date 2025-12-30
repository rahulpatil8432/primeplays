package com.rkonline.android.model;

public class NotificationModel {

    private String title;
    private String message;
    private String date;
    private String docId;
    private boolean hasUserIds;

    public NotificationModel(String title, String message, String date,String docId, boolean hasUserIds) {
        this.title = title;
        this.message = message;
        this.date = date;
        this.docId = docId;
        this.hasUserIds = hasUserIds;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isHasUserIds() {
        return hasUserIds;
    }

    public void setHasUserIds(boolean hasUserIds) {
        this.hasUserIds = hasUserIds;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }
}
