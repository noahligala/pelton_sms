package com.example.peltondata;

import android.app.Application;

import com.example.peltondata.utils.UserManager;

public class Pelton extends Application {
    private UserManager userManager;

    @Override
    public void onCreate() {
        super.onCreate();
        userManager = new UserManager();
    }

    public UserManager getUserManager() {
        return userManager;
    }
}
