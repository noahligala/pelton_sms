package com.example.peltondata.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peltondata.R;
import com.example.peltondata.Transaction;
import com.example.peltondata.adapters.TransactionAdapter;
import com.example.peltondata.viewmodels.TransactionViewModel;

import java.util.ArrayList;

public class TransactionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private TransactionViewModel transactionViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TransactionAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        transactionViewModel.getTransactions().observe(this, transactions -> {
            // Update the adapter with the new list of transactions
            adapter.setTransactions(transactions);
        });

        // Retrieve the isAdmin flag from the Intent
        boolean isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        if (isAdmin) {
            String serverUrl = "https://pelton.co.ke/config.php";
            transactionViewModel.fetchTransactionsFromServer(serverUrl);
        } else {
            // Normal flow for non-admin users (populate with local data)
            loadLocalTransactions();
        }
    }

    private void loadLocalTransactions() {
        // Add some dummy data or retrieve it from a local data source
        transactionViewModel.addTransaction(new Transaction("T001", 150.0, "2024-05-20", "14:30", "C001"));
        transactionViewModel.addTransaction(new Transaction("T002", 200.0, "2024-05-21", "15:00", "C002"));
        // Add more transactions as needed
    }
}
