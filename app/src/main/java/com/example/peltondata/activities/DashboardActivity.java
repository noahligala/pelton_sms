package com.example.peltondata.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.peltondata.R;
import com.example.peltondata.fragments.LeaveFragment;
import com.example.peltondata.fragments.ProfileFragment;
import com.example.peltondata.fragments.ShiftManagementFragment;
import com.example.peltondata.fragments.TransactionFragment;
import com.example.peltondata.fragments.UserManagementFragment;
import com.example.peltondata.services.SmsReceiver;
import com.example.peltondata.viewmodels.TransactionViewModel;

import java.util.Objects;

public class DashboardActivity extends AppCompatActivity {

    private boolean isAdmin;
    private SmsReceiver smsReceiver;
    private static final String PREFS_NAME = "ShiftPrefs";
    private static final String KEY_CHECKED_IN = "checked_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize ViewModel
        TransactionViewModel transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Set the ViewModel in the SmsReceiver
        SmsReceiver.setTransactionViewModel(transactionViewModel);

        // Initialize SMS Receiver
        smsReceiver = new SmsReceiver();

        // Register the SMS Receiver
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, filter);

        isAdmin = getIntent().getBooleanExtra("isAdmin", false);
        TextView roleTextView = findViewById(R.id.roleTextView);

        if (isAdmin) {
            roleTextView.setText("Server Mode: Admin User");
            // Set up the app as a server
            setupServer();
        } else {
            roleTextView.setText("Client Mode: Regular User");
            // Set up the app as a client
            setupClient();
        }

        // Adjust visibility of system button
        adjustSystemButtonVisibility();

        setupToolbar();

        // Simulate SMS reception for testing
        simulateSmsReception();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the SMS Receiver
        unregisterReceiver(smsReceiver);
    }

    private void setupServer() {
        // Implement server setup code here
    }

    private void setupClient() {
        // Implement client setup code here
    }

    private void adjustSystemButtonVisibility() {
        LinearLayout systemButton = findViewById(R.id.systemButton);
        if (isAdmin) {
            systemButton.setVisibility(View.VISIBLE);
        } else {
            systemButton.setVisibility(View.GONE);
        }
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void simulateSmsReception() {
        // Create a fake SMS message
        String messageBody = "PA123456 Confirmed. Ksh 1,000.00 paid to Rubis Kitale from Tom Musyoka on 24/4/21 at 3:40 PM New M-PESA balance is Ksh5,941.22.";
        String senderNumber = "MPESA";

        // Create an intent that would be received by the SmsReceiver
        Intent intent = new Intent();
        intent.setAction("android.provider.Telephony.SMS_RECEIVED");

        // Create the PDU array with the fake message
        SmsMessage[] smsMessages = new SmsMessage[1];
        smsMessages[0] = SmsMessage.createFromPdu(createFakePdu(messageBody, senderNumber), "3gpp");

        Bundle bundle = new Bundle();
        bundle.putSerializable("pdus", new Object[]{createFakePdu(messageBody, senderNumber)});
        bundle.putString("format", "3gpp");
        intent.putExtras(bundle);

        // Call onReceive directly
        smsReceiver.onReceive(this, intent);
    }

    private byte[] createFakePdu(String messageBody, String senderNumber) {
        // Placeholder for PDU creation logic
        // This implementation may vary based on the expected PDU format.
        // Refer to GSM 03.40 specification for correct PDU creation.
        return new byte[]{};
    }

    public void navigateToShiftManagement(View view) {
        try {
            loadFragment(new ShiftManagementFragment(), "Shift Management");
        } catch (Exception e) {
            handleNavigationError(e);
        }
    }

    public void navigateToSMSModule(View view) {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean isCheckedIn = sharedPreferences.getBoolean(KEY_CHECKED_IN, false);

            if (isCheckedIn) {
                loadFragment(new TransactionFragment(), "Transactions");
            } else {
                loadFragment(new ShiftManagementFragment(), "Shift Management");
            }
        } catch (Exception e) {
            handleNavigationError(e);
        }
    }

    public void navigateToLeaveSchedule(View view) {
        try {
            loadFragment(new LeaveFragment(), "Leave Schedule");
        } catch (Exception e) {
            handleNavigationError(e);
        }
    }

    public void navigateToProfileManagement(View view) {
        try {
            loadFragment(new ProfileFragment(), "Profile Management");
        } catch (Exception e) {
            handleNavigationError(e);
        }
    }

    public void navigateToSettings(View view) {
        try {
            loadFragment(new UserManagementFragment(), "Settings");
        } catch (Exception e) {
            handleNavigationError(e);
        }
    }

    private void loadFragment(Fragment fragment, String title) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        findViewById(R.id.dashboard_buttons).setVisibility(View.GONE);

        // Update the toolbar title and add back button
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void handleNavigationError(Exception e) {
        e.printStackTrace();
        Toast.makeText(this, "An error occurred while navigating. Please try again.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            findViewById(R.id.fragment_container).setVisibility(View.GONE);
            findViewById(R.id.dashboard_buttons).setVisibility(View.VISIBLE);

            // Reset the toolbar
            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.dashboard);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        } else {
            super.onBackPressed();
        }
    }
}
