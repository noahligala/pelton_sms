package com.example.peltondata.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peltondata.R;
import com.example.peltondata.Transaction;
import com.example.peltondata.adapters.TransactionAdapter;
import com.example.peltondata.services.SmsReceiver;
import com.example.peltondata.viewmodels.TransactionViewModel;

import java.util.List;

public class TransactionFragment extends Fragment {

    private TransactionViewModel transactionViewModel;
    private TransactionAdapter transactionAdapter;
    private SmsReceiver smsReceiver;

    private static final String PREFS_NAME = "ShiftPrefs";
    private static final String KEY_CHECKED_IN = "checked_in";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        smsReceiver = new SmsReceiver();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isCheckedIn = sharedPreferences.getBoolean(KEY_CHECKED_IN, false);

        if (!isCheckedIn) {
            // Navigate to ShiftManagementFragment if not checked in
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ShiftManagementFragment())
                    .addToBackStack(null)
                    .commit();
            return;
        }

        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        SmsReceiver.setTransactionViewModel(transactionViewModel);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.transaction_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionAdapter = new TransactionAdapter(getContext(), null);
        recyclerView.setAdapter(transactionAdapter);

        transactionViewModel.getTransactions().observe(getViewLifecycleOwner(), new Observer<List<Transaction>>() {
            @Override
            public void onChanged(List<Transaction> transactions) {
                transactionAdapter.setTransactions(transactions);
            }
        });

        // Process inbox messages to load any existing transactions
        smsReceiver.processInboxMessages(getContext());

        return view;
    }
}
