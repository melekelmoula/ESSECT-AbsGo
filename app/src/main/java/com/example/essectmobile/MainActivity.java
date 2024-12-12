package com.example.essectmobile;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.essectmobile.Admin.AdminMain;
import com.example.essectmobile.Agent.AgentMain;
import com.example.essectmobile.Professor.ProfessorMain;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText etEmail, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Check if the user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {fetchUserRole(currentUser.getUid());}

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            System.out.println("Fetching FCM registration token failed");
                            return;
                        }

                        String token = task.getResult();
                        Toast.makeText(MainActivity.this, "device token :"+token, Toast.LENGTH_SHORT).show();
                    }
                });

        btnLogin.setOnClickListener(v -> loginUser());
    }


    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        fetchUserRole(user.getUid());
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserRole(String uid) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String role = document.getString("role");
                            if ("Admin".equals(role)) {
                                startActivity(new Intent(MainActivity.this, AdminMain.class));
                            } else if ("Agent".equals(role)) {
                                startActivity(new Intent(MainActivity.this, AgentMain.class));
                            } else if ("Professor".equals(role)) {
                                startActivity(new Intent(MainActivity.this, ProfessorMain.class));
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "User not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Error fetching user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
