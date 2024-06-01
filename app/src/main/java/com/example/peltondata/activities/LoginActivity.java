package com.example.peltondata.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.peltondata.Pelton;
import com.example.peltondata.R;
import com.example.peltondata.utils.UserManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.view.Window;
import android.view.WindowManager;
import java.util.Calendar;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout usernameLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private Button loginButton;

    private UserManager userManager;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set the status bar color to white
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.white));

        userManager = ((Pelton) getApplication()).getUserManager();

        usernameLayout = findViewById(R.id.usernameInputLayout);
        passwordLayout = findViewById(R.id.passwordInputLayout);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.editTextPassword);

        loginButton = findViewById(R.id.login_button);

        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);

        // Check if the user is already logged in and if the login has expired
        if (isLoggedIn() && !isLoginExpired()) {
            redirectUser();
        }

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (validateInputs(username, password)) {
                if (userManager.validateCredentials(username, password)) {
                    com.example.peltondata.User user = userManager.getUser(username);
                    String welcomeMessage = user.isAdmin() ? "Welcome Admin User" : "Welcome Regular User";
                    Toast.makeText(LoginActivity.this, welcomeMessage, Toast.LENGTH_SHORT).show();

                    // Save login time
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong("loginTime", System.currentTimeMillis());
                    editor.apply();

                    redirectUser();
                } else {
                    showValidationError(usernameLayout, "Invalid Username or Password");
                    showValidationError(passwordLayout, "Invalid Username or Password");
                }
            }
        });
    }

    private boolean validateInputs(String username, String password) {
        boolean isValid = true;

        if (username.isEmpty()) {
            showValidationError(usernameLayout, "Username is required");
            isValid = false;
        } else {
            usernameLayout.setError(null);
        }

        if (password.isEmpty()) {
            showValidationError(passwordLayout, "Password is required");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        return isValid;
    }

    private void showValidationError(TextInputLayout layout, String message) {
        layout.setError(message);
        layout.requestFocus();
    }

    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private boolean isLoginExpired() {
        long loginTime = sharedPreferences.getLong("loginTime", 0);
        if (loginTime == 0) {
            // No previous login
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - loginTime;
        long elapsedHours = elapsedTime / (60 * 60 * 1000); // Convert milliseconds to hours

        return elapsedHours >= 24; // Check if more than 24 hours have elapsed since last login
    }

    private void redirectUser() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
