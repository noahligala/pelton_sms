package com.example.peltondata.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.peltondata.Pelton;
import com.example.peltondata.R;
import com.example.peltondata.utils.UserManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // Define a tag for logging
    private static final int PERMISSION_REQUEST_CODE = 1;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: MainActivity created"); // Log a debug message

        // Get UserManager from MyApplication
        userManager = ((Pelton) getApplication()).getUserManager();

        // Check for SMS permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, PERMISSION_REQUEST_CODE);
        } else {
            // Permissions are already granted
            initApp();
        }
    }

    private void initApp() {
        // For demonstration, let's start the LoginActivity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, initialize the app
                initApp();
            } else {
                // Permission denied, handle accordingly
                Log.e(TAG, "SMS permissions denied");
            }
        }
    }
}
