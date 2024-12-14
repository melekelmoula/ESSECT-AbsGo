package com.example.essectmobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;

import androidx.appcompat.app.AppCompatActivity;

import com.example.essectmobile.Admin.AdminMain;
import com.example.essectmobile.Agent.AgentMain;
import com.example.essectmobile.Professor.ProfessorMain;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize ViewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Observe user role changes
        mainViewModel.getUserRoleLiveData().observe(this, role -> {
            if (role != null) {
                navigateToRoleActivity(role);
            }
        });

        // Observe device token
        mainViewModel.getDeviceTokenLiveData().observe(this, token -> {
            if (token != null) {
                Toast.makeText(this, "Device token: " + token, Toast.LENGTH_SHORT).show();
            }
        });

        // Check if the user is already signed in
        mainViewModel.checkCurrentUser();

        // Fetch device token
        mainViewModel.fetchDeviceToken();

        // Login button click listener
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            mainViewModel.loginUser(email, password);
        });
    }

    // Navigate to the respective role activity
    private void navigateToRoleActivity(String role) {
        Intent intent;
        switch (role) {
            case "Admin":
                intent = new Intent(MainActivity.this, AdminMain.class);
                break;
            case "Agent":
                intent = new Intent(MainActivity.this, AgentMain.class);
                break;
            case "Professor":
                intent = new Intent(MainActivity.this, ProfessorMain.class);
                break;
            default:
                Toast.makeText(MainActivity.this, "Unknown role", Toast.LENGTH_SHORT).show();
                return;
        }
        startActivity(intent);
    }
}
