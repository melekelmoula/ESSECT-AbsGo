package com.example.essectmobile.Professor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.essectmobile.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;
import java.util.Map;

public class ProfessorMain extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TableLayout tblNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        saveProfessorFcmToken(); // Save the FCM token after login

        tblNotifications = findViewById(R.id.tblNotifications);

        // Fetch notifications for the current user
        fetchNotifications();
    }

    private void fetchNotifications() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("notification")
                .document(uid)  // Get the document with the UID of the current user
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            Object notificationsObj = document.get("notifications");
                            if (notificationsObj != null && notificationsObj instanceof List) {
                                List<Map<String, Object>> notifications = (List<Map<String, Object>>) notificationsObj;

                                if (notifications.isEmpty()) {
                                    Toast.makeText(this, "No notifications found", Toast.LENGTH_SHORT).show();
                                }

                                // Iterate through the notifications and add them to the TableLayout
                                for (int i = 0; i < notifications.size(); i++) {
                                    Map<String, Object> notification = notifications.get(i);

                                    String currentDate = (String) notification.get("currentDate");
                                    String group = (String) notification.get("group");
                                    String professorName = (String) notification.get("professorName");
                                    String room = (String) notification.get("room");
                                    String subject = (String) notification.get("subject");
                                    String timeSlot = (String) notification.get("timeSlot");
                                    String AbsenceNo = (String) notification.get("AbsenceNo");

                                    notification.put("index", String.valueOf(i));

                                    // Check for the "reclamation" field
                                    String reclamation = (String) notification.get("reclamation");

                                    // Create a new table row for each notification
                                    TableRow row = new TableRow(this);

                                    // Add the index (e.g., 0, 1, 2...) to the row
                                    row.addView(createTextView(AbsenceNo));  // Index as TextView
                                    row.addView(createTextView(currentDate));
                                    row.addView(createTextView(group));
                                    row.addView(createTextView(professorName));
                                    row.addView(createTextView(room));
                                    row.addView(createTextView(subject));
                                    row.addView(createTextView(timeSlot));

                                    // Create a button for reclamation
                                    Button reclamationButton = new Button(this);

                                    // If reclamation field is not found or is empty, disable the button
                                    if (reclamation == null || reclamation.isEmpty()) {
                                        reclamationButton.setText("Reclamation");
                                        reclamationButton.setEnabled(true); // Disable the button
                                        reclamationButton.setOnClickListener(v -> openReclamationForm(notification));

                                    } else {
                                        reclamationButton.setText(reclamation);
                                        reclamationButton.setEnabled(false); // Disable the button
                                    }
                                    // Add the button to the row
                                    row.addView(reclamationButton);

                                    // Add the row to the TableLayout
                                    tblNotifications.addView(row);
                                }
                            } else {
                                Toast.makeText(this, "No notifications array found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Error fetching notifications", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openReclamationForm(Map<String, Object> notification) {
        Intent intent = new Intent(this, ProfessorReclamation.class);
        // Pass the correct notification data and the index
        intent.putExtra("AbsenceNo", (String) notification.get("AbsenceNo")); // Use a more meaningful key like "index" instead of "ID"
        intent.putExtra("professorName", (String) notification.get("professorName"));
        intent.putExtra("subject", (String) notification.get("subject"));
        intent.putExtra("group", (String) notification.get("group"));
        intent.putExtra("timeSlot", (String) notification.get("timeSlot"));
        intent.putExtra("agentUid", (String) notification.get("agent uid"));

        startActivity(intent);
    }


    // Helper method to create TextViews for each column in the TableRow
    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 8, 16, 8);
        textView.setTextSize(16);
        return textView;
    }

    private void saveProfessorFcmToken() {
        // Get the current user UID
        String uid = mAuth.getCurrentUser().getUid();

        // Get the FCM token from Firebase Cloud Messaging
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String fcmToken = task.getResult(); // Get the token
                        if (fcmToken != null) {
                            // Save the token in Firestore under the user's document
                            db.collection("users").document(uid)
                                    .update("fcmToken", fcmToken)
                                    .addOnSuccessListener(aVoid -> {
                                        // Successfully saved the token in Firestore
                                        Toast.makeText(ProfessorMain.this, "FCM token saved successfully!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure to save token
                                        Toast.makeText(ProfessorMain.this, "Error saving FCM token: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(ProfessorMain.this, "Error getting FCM token: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
