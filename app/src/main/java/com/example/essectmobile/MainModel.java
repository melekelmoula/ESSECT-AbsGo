package com.example.essectmobile;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainModel {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Context context;

    public MainModel(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Function to check the current user and fetch role
    public void checkCurrentUser(MutableLiveData<String> userRoleLiveData) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchUserRole(currentUser.getUid(), userRoleLiveData);
        }
    }

    // Function to log in the user
    public void loginUser(String email, String password, MutableLiveData<String> userRoleLiveData) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        fetchUserRole(user.getUid(), userRoleLiveData);
                    } else {
                        Toast.makeText(context, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Fetch user role from Firestore
    private void fetchUserRole(String uid, MutableLiveData<String> userRoleLiveData) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String role = document.getString("role");
                            userRoleLiveData.setValue(role);
                        } else {
                            Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Error fetching user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Fetch device token for push notifications
    public void fetchDeviceToken(MutableLiveData<String> tokenLiveData) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        System.out.println("Fetching FCM registration token failed");
                        return;
                    }

                    String token = task.getResult();
                    tokenLiveData.setValue(token);
                });
    }
}
