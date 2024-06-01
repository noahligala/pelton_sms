package com.example.peltondata.models;

import java.io.Serializable;

public class CheckInOutRecord implements Serializable {
    private String action;
    private String timestamp;

    public CheckInOutRecord(String action, String timestamp) {
        this.action = action;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
