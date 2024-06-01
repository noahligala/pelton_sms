package com.example.peltondata.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.peltondata.models.CheckInOutRecord;
import com.example.peltondata.R;
import com.example.peltondata.adapters.CheckInOutAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShiftManagementFragment extends Fragment {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final double BUSINESS_LATITUDE = 1.0185434596768685;  // Replace with actual latitude
    private static final double BUSINESS_LONGITUDE = 35.02890283459279; // Replace with actual longitude
    private static final float LOCATION_RADIUS = 1000; // Radius in meters

    private FusedLocationProviderClient fusedLocationClient;
    private TextView locationStatus;
    private CheckInOutAdapter checkInOutAdapter;
    private List<CheckInOutRecord> checkInOutRecords;

    private static final String PREFS_NAME = "ShiftPrefs";
    private static final String KEY_CHECKED_IN = "checked_in";
    private static final String KEY_HISTORY = "check_in_out_history";

    private boolean isCheckedIn;

    public ShiftManagementFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shift_management, container, false);

        locationStatus = view.findViewById(R.id.location_status);
        Button checkInButton = view.findViewById(R.id.check_in_button);
        Button checkOutButton = view.findViewById(R.id.check_out_button);
        RecyclerView historyRecyclerView = view.findViewById(R.id.history_recycler_view);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        checkInOutRecords = loadHistoryFromPreferences();
        checkInOutAdapter = new CheckInOutAdapter(checkInOutRecords);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        historyRecyclerView.setAdapter(checkInOutAdapter);

        isCheckedIn = loadCheckedInStatus();

        checkInButton.setOnClickListener(v -> {
            if (isCheckedIn) {
                locationStatus.setText("You are already checked in. Please check out first.");
            } else {
                checkLocationAndPerformAction("Check In");
            }
        });

        checkOutButton.setOnClickListener(v -> {
            if (!isCheckedIn) {
                locationStatus.setText("You are not checked in. Please check in first.");
            } else {
                checkLocationAndPerformAction("Check Out");
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isCheckedIn = loadCheckedInStatus(); // Reload checked-in status onResume
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!isCheckedIn) {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(KEY_CHECKED_IN); // Remove the checked-in status when fragment is destroyed if user is not checked in
            editor.apply();
        }
    }

    private void checkLocationAndPerformAction(String action) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(), BUSINESS_LATITUDE, BUSINESS_LONGITUDE, results);
                float distanceInMeters = results[0];

                if (distanceInMeters <= LOCATION_RADIUS) {
                    performAction(action);
                } else {
                    locationStatus.setText("You are not within the business premises");
                }
            } else {
                locationStatus.setText("Could not fetch location. Try again.");
            }
        });
    }

    private void performAction(String action) {
        locationStatus.setText("Successfully " + action.toLowerCase() + "ed");

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        checkInOutRecords.add(0, new CheckInOutRecord(action, timestamp));  // Add new record at the beginning
        checkInOutAdapter.setRecords(checkInOutRecords);
        saveHistoryToPreferences(checkInOutRecords);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if ("Check In".equals(action)) {
            isCheckedIn = true;
            editor.putBoolean(KEY_CHECKED_IN, true);
        } else if ("Check Out".equals(action)) {
            isCheckedIn = false;
            editor.putBoolean(KEY_CHECKED_IN, false);
        }
        editor.apply();
    }

    private void saveHistoryToPreferences(List<CheckInOutRecord> records) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(records);
            String serializedRecords = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT);
            editor.putString(KEY_HISTORY, serializedRecords);
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.apply();
    }

    private List<CheckInOutRecord> loadHistoryFromPreferences() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String serializedRecords = sharedPreferences.getString(KEY_HISTORY, null);
        if (serializedRecords != null) {
            try {
                byte[] data = android.util.Base64.decode(serializedRecords, android.util.Base64.DEFAULT);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                return (List<CheckInOutRecord>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    private boolean loadCheckedInStatus() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_CHECKED_IN, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationAndPerformAction("Check In");
            } else {
                locationStatus.setText("Location permission denied");
            }
        }
    }
}
