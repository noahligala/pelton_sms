package com.example.peltondata.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class SmsService extends Service {

    private SmsObserver smsObserver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start listening for changes in the SMS inbox
        smsObserver = new SmsObserver(new Handler(), this);
        getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, smsObserver);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the content observer when the service is destroyed
        if (smsObserver != null) {
            getContentResolver().unregisterContentObserver(smsObserver);
        }
    }
}

