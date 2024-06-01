package com.example.peltondata.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peltondata.R;
import com.example.peltondata.Transaction;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final Context context;
    private List<Transaction> transactionList;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.transactionCodeTextView.setText(transaction.getTransactionCode());
        holder.transactionAmountTextView.setText(String.valueOf(transaction.getTransactionAmount()));
        holder.transactionDateTextView.setText(transaction.getTransactionDate());
        holder.transactionTimeTextView.setText(transaction.getTransactionTime());
        holder.customerIDTextView.setText(transaction.getCustomerID());
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    // Method to update the list of transactions
    public void setTransactions(List<Transaction> transactions) {
        this.transactionList = transactions;
        notifyDataSetChanged();
    }

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
                e.printStackTrace();
            }
            return transactions;
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            setTransactions(transactions);
        }
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView transactionCodeTextView, transactionAmountTextView, transactionDateTextView, transactionTimeTextView, customerIDTextView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionCodeTextView = itemView.findViewById(R.id.transaction_code);
            transactionAmountTextView = itemView.findViewById(R.id.transaction_amount);
            transactionDateTextView = itemView.findViewById(R.id.transaction_date);
            transactionTimeTextView = itemView.findViewById(R.id.transaction_time);
            customerIDTextView = itemView.findViewById(R.id.customer_id);
        }
    }
}
