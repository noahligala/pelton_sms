package com.example.peltondata.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.peltondata.Transaction;
import com.example.peltondata.ServerClient;
import com.example.peltondata.viewmodels.TransactionViewModel;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";
    private static TransactionViewModel transactionViewModel;
    private ExecutorService executorService;
    private Handler mainHandler;
    private static Set<String> processedTransactionCodes = new HashSet<>();
    private static boolean isStaff = false;

    public SmsReceiver() {
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static void setTransactionViewModel(TransactionViewModel viewModel) {
        transactionViewModel = viewModel;
    }

    public static void setIsStaff(boolean isStaff) {
        SmsReceiver.isStaff = isStaff;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isStaff) {
            Log.d(TAG, "SmsReceiver: Skipping SMS processing for staff.");
            return;
        }

        Bundle bundle = intent.getExtras();
        Log.d(TAG, "SmsReceiver: SMS Received");

        if (bundle != null) {
            executorService.submit(() -> {
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < messages.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], bundle.getString("format"));
                        String sender = messages[i].getOriginatingAddress();
                        Log.d(TAG, "SmsReceiver: SMS from " + sender);
                        if (sender != null && sender.equals("MPESA")) {
                            Transaction transaction = parseTransaction(messages[i].getMessageBody());
                            if (transaction != null && !isDuplicateTransaction(transaction.getTransactionCode())) {
                                ServerClient.postTransaction(transaction);
                                mainHandler.post(() -> {
                                    transactionViewModel.addTransaction(transaction);
                                    Log.d(TAG, "Transaction Code: " + transaction.getTransactionCode());
                                    Log.d(TAG, "Transaction Amount: " + transaction.getTransactionAmount());
                                    Log.d(TAG, "Transaction Date: " + transaction.getTransactionDate());
                                    Log.d(TAG, "Transaction Time: " + transaction.getTransactionTime());
                                    Log.d(TAG, "Customer ID: " + transaction.getCustomerID());
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception: " + e.getMessage());
                }
            });
        }
    }

    private Transaction parseTransaction(String message) {
        String transactionCode = "";
        double transactionAmount = 0.0;
        String transactionDate = "";
        String transactionTime = "";
        String customerID = "";

        try {
            Pattern pattern = Pattern.compile(
                    "(\\w+\\d+) Confirmed\\.You have received Ksh([\\d,]+\\.\\d{2}) from ([\\w\\s]+ \\d+) on (\\d{1,2}/\\d{1,2}/\\d{2}) at (\\d{1,2}:\\d{2} [APM]{2})"
            );
            Matcher matcher = pattern.matcher(message);

            if (matcher.find()) {
                transactionCode = matcher.group(1);
                transactionAmount = Double.parseDouble(matcher.group(2).replace(",", ""));
                customerID = matcher.group(3);
                transactionDate = matcher.group(4);
                transactionTime = matcher.group(5);
            }
        } catch (Exception e) {
            Log.e(TAG, "parseTransaction: Exception: " + e.getMessage());
        }

        Log.d(TAG, "Parsed Transaction Code: " + transactionCode);
        Log.d(TAG, "Parsed Transaction Amount: " + transactionAmount);
        Log.d(TAG, "Parsed Transaction Date: " + transactionDate);
        Log.d(TAG, "Parsed Transaction Time: " + transactionTime);
        Log.d(TAG, "Parsed Customer ID: " + customerID);

        return new Transaction(transactionCode, transactionAmount, transactionDate, transactionTime, customerID);
    }

    private boolean isDuplicateTransaction(String transactionCode) {
        synchronized (processedTransactionCodes) {
            if (processedTransactionCodes.contains(transactionCode)) {
                Log.d(TAG, "Duplicate transaction detected: " + transactionCode);
                return true;
            } else {
                processedTransactionCodes.add(transactionCode);
                return false;
            }
        }
    }

    public void processInboxMessages(Context context) {
        executorService.submit(() -> {
            try {
                Uri uri = Uri.parse("content://sms/inbox");
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));

                        if ("MPESA".equals(address)) {
                            Transaction transaction = parseTransaction(body);
                            if (transaction != null && !isDuplicateTransaction(transaction.getTransactionCode())) {
                                ServerClient.postTransaction(transaction);
                                mainHandler.post(() -> {
                                    transactionViewModel.addTransaction(transaction);
                                    Log.d(TAG, "Inbox Transaction Code: " + transaction.getTransactionCode());
                                    Log.d(TAG, "Inbox Transaction Amount: " + transaction.getTransactionAmount());
                                    Log.d(TAG, "Inbox Transaction Date: " + transaction.getTransactionDate());
                                    Log.d(TAG, "Inbox Transaction Time: " + transaction.getTransactionTime());
                                    Log.d(TAG, "Inbox Customer ID: " + transaction.getCustomerID());
                                });
                            }
                        }
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        });
    }
}
