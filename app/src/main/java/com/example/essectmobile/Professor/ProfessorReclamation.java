package com.example.essectmobile.Professor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.essectmobile.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfessorReclamation extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText etReclamationText;
    private Button btnSubmitReclamation;
    private String professorName;
    private String subject;
    private String group;
    private String timeSlot;
    private String agentUid;
    private String absenceno;
    private String index;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reclamation);

        db = FirebaseFirestore.getInstance();

        etReclamationText = findViewById(R.id.etReclamationText);
        btnSubmitReclamation = findViewById(R.id.btnSubmitReclamation);

        // Get data passed from the previous activity (notification data)
        professorName = getIntent().getStringExtra("professorName");
        subject = getIntent().getStringExtra("subject");
        group = getIntent().getStringExtra("group");
        timeSlot = getIntent().getStringExtra("timeSlot");
        agentUid = getIntent().getStringExtra("agentUid");
        index = getIntent().getStringExtra("index");
        absenceno = getIntent().getStringExtra("AbsenceNo");

        TextView tvNotificationDetails = findViewById(R.id.tvNotificationDetails);
        tvNotificationDetails.setText(absenceno+"Professor: " + professorName + "\nSubject: " + subject +
                "\nGroup: " + group + "\nTime Slot: " + timeSlot);

        // Handle submit reclamation
        btnSubmitReclamation.setOnClickListener(v -> submitReclamation());
    }

    private void submitReclamation() {
        String reclamationText = etReclamationText.getText().toString().trim();

        if (reclamationText.isEmpty()) {
            Toast.makeText(this, "Please enter your reclamation", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        // Prepare data to be saved to Firestore
        Map<String, Object> reclamation = new HashMap<>();
        reclamation.put("reclamationText", reclamationText);
        reclamation.put("professorName", professorName);
        reclamation.put("subject", subject);
        reclamation.put("group", group);
        reclamation.put("timeSlot", timeSlot);
        reclamation.put("agentUid", agentUid);
        reclamation.put("date", currentDate); // Add the current date
        reclamation.put("absenceno", absenceno); // Add the current date

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String reclamationId = currentUser.getUid(); // Get the UID of the current user

        // Reference to the "reclamations" collection under the unique reclamationId
        db.collection("reclamations")
                .document(reclamationId) // Document ID = unique identifier (e.g., based on professor, subject, group)
                .get() // Check if the document exists
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document exists, update the reclamation array or details
                        db.collection("reclamations")
                                .document(reclamationId)
                                .update("reclamations", FieldValue.arrayUnion(reclamation)) // Add the new reclamation
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ProfessorReclamation.this, "Reclamation added successfully", Toast.LENGTH_SHORT).show();
                                    finish(); // Close the activity after submission
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ProfessorReclamation.this, "Error updating reclamation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Document does not exist, create it with the reclamation array
                        List<Map<String, Object>> reclamationsList = new ArrayList<>();
                        reclamationsList.add(reclamation); // Add first reclamation

                        db.collection("reclamations")
                                .document(reclamationId)
                                .set(new HashMap<String, Object>() {{
                                    put("reclamations", reclamationsList);
                                }})
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ProfessorReclamation.this, "Reclamation submitted successfully", Toast.LENGTH_SHORT).show();
                                    finish(); // Close the activity after submission
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ProfessorReclamation.this, "Error saving reclamation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                    updateNotification(currentUser.getUid(),absenceno);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfessorReclamation.this, "Error fetching document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateNotification(String userId, String absenceno) {
        // Reference to the "notifications" collection
        db.collection("notification")
                .document(userId) // Document ID = user ID
                .get()
                .addOnSuccessListener(notificationDoc -> {
                    if (notificationDoc.exists()) {
                        // Get the array of notifications
                        List<Map<String, Object>> notifications = (List<Map<String, Object>>) notificationDoc.get("notifications");

                        if (notifications != null && !notifications.isEmpty()) {
                            // Iterate through notifications to find the one with the matching AbsenceNo
                            for (Map<String, Object> notification : notifications) {
                                if (absenceno.equals(notification.get("AbsenceNo"))) {
                                    // Add the "reclamation" field with "pending" status
                                    notification.put("reclamation", "pending");

                                    // Update the notifications in the Firestore
                                    db.collection("notification")
                                            .document(userId)
                                            .update("notifications", notifications)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(ProfessorReclamation.this, "Notification updated", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(ProfessorReclamation.this, "Error updating notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                    break; // Break the loop once the matching notification is found and updated
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfessorReclamation.this, "Error fetching notifications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}
