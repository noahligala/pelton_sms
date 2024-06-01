package com.example.peltondata.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

// Make sure to import the Transaction class
import com.example.peltondata.R;
import com.example.peltondata.Transaction;

public class TransactionDetailsActivity extends AppCompatActivity {

    private TextView transactionCodeTextView, transactionAmountTextView, transactionDateTextView, transactionTimeTextView, customerIDTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_details);

        transactionCodeTextView = findViewById(R.id.transaction_code);
        transactionAmountTextView = findViewById(R.id.transaction_amount);
        transactionDateTextView = findViewById(R.id.transaction_date);
        transactionTimeTextView = findViewById(R.id.transaction_time);
        customerIDTextView = findViewById(R.id.customer_id);

        // Retrieve the transaction object passed through the intent
        Transaction transaction = (Transaction) getIntent().getSerializableExtra("transaction");
        if (transaction != null) {
            displayTransactionDetails(transaction);
        }
    }

    private void displayTransactionDetails(Transaction transaction) {
        transactionCodeTextView.setText(transaction.getTransactionCode());
        transactionAmountTextView.setText(String.valueOf(transaction.getTransactionAmount()));
        transactionDateTextView.setText(transaction.getTransactionDate());
        transactionTimeTextView.setText(transaction.getTransactionTime());
        customerIDTextView.setText(transaction.getCustomerID());
    }
}
