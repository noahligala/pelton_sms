package com.example.peltondata;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerClient {
    private static final String TAG = "ServerClient"; // Define a tag for logging

    public static void postTransaction(Transaction transaction) {
        new PostTransactionTask().execute(transaction);
    }

    private static class PostTransactionTask extends AsyncTask<Transaction, Void, Void> {
        @Override
        protected Void doInBackground(Transaction... transactions) {
            Transaction transaction = transactions[0];
            HttpURLConnection conn = null;
            BufferedReader reader = null;

            try {
                if (transaction == null || !isValidTransaction(transaction)) {
                    Log.e(TAG, "Invalid transaction data, not sending request.");
                    return null;
                }

                URL url = new URL("https://pelton.co.ke/sms/config.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject json = new JSONObject();
                json.put("transactionCode", transaction.getTransactionCode());
                json.put("transactionAmount", transaction.getTransactionAmount());
                json.put("transactionDate", transaction.getTransactionDate());
                json.put("transactionTime", transaction.getTransactionTime());
                json.put("customerID", transaction.getCustomerID());

                // Log the JSON object before sending
                Log.d(TAG, "JSON to be sent: " + json.toString());

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Server response code: " + responseCode);

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                Log.d(TAG, "Server response: " + response.toString());

            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage(), e);

            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing reader: " + e.getMessage(), e);
                    }
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }

        private boolean isValidTransaction(Transaction transaction) {
            return transaction.getTransactionCode() != null && !transaction.getTransactionCode().isEmpty()
                    && transaction.getTransactionAmount() > 0
                    && transaction.getTransactionDate() != null && !transaction.getTransactionDate().isEmpty()
                    && transaction.getTransactionTime() != null && !transaction.getTransactionTime().isEmpty()
                    && transaction.getCustomerID() != null && !transaction.getCustomerID().isEmpty();
        }
    }
}
