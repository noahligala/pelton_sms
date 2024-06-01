package com.example.peltondata.viewmodels;

import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.peltondata.Transaction;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TransactionViewModel extends ViewModel {

    private final MutableLiveData<List<Transaction>> transactionsLiveData;
    private static final String TAG = "TransactionViewModel";

    public TransactionViewModel() {
        transactionsLiveData = new MutableLiveData<>();
        // Initialize with an empty list
        transactionsLiveData.setValue(new ArrayList<>());
    }

    // Method to retrieve the LiveData object for observing changes in the transaction list
    public LiveData<List<Transaction>> getTransactions() {
        return transactionsLiveData;
    }

    // Method to add a new transaction to the list
    public void addTransaction(Transaction transaction) {
        List<Transaction> transactions = transactionsLiveData.getValue();
        if (transactions != null) {
            transactions.add(transaction);
            transactionsLiveData.setValue(transactions);
        }
    }

    // Method to fetch transactions from the server
    public void fetchTransactionsFromServer(String serverUrl) {
        new FetchTransactionsTask().execute(serverUrl);
    }

    private class FetchTransactionsTask extends AsyncTask<String, Void, List<Transaction>> {

        @Override
        protected List<Transaction> doInBackground(String... params) {
            String serverUrl = params[0];
            List<Transaction> transactions = new ArrayList<>();
            try {
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.getString("status").equals("success")) {
                    JSONArray transactionsArray = jsonResponse.getJSONArray("transactions");
                    for (int i = 0; i < transactionsArray.length(); i++) {
                        JSONObject transactionObject = transactionsArray.getJSONObject(i);
                        String transactionCode = transactionObject.getString("transactionCode");
                        double transactionAmount = transactionObject.getDouble("transactionAmount");
                        String transactionDate = transactionObject.getString("transactionDate");
                        String transactionTime = transactionObject.getString("transactionTime");
                        String customerID = transactionObject.getString("customerID");

                        Transaction transaction = new Transaction(transactionCode, transactionAmount, transactionDate, transactionTime, customerID);
                        transactions.add(transaction);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching transactions: ", e);
            }
            return transactions;
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            transactionsLiveData.setValue(transactions);
        }
    }
}
