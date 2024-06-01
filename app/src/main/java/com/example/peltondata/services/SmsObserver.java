package com.example.peltondata.services;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;

public class SmsObserver extends ContentObserver {

    private Context context;

    public SmsObserver(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        // Handle inbox changes here, e.g., extract new messages and process them
        // You can use the same logic as in the BroadcastReceiver to extract and process messages
        // Ensure to run any UI-related operations on the main thread if needed
    }
}
