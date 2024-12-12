package com.example.essectmobile.Admin;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.essectmobile.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminReclamationsList extends AppCompatActivity {

    private TableLayout tableLayoutNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reclamations_full_list);

        tableLayoutNotifications = findViewById(R.id.tableLayoutReclamations);

        // Get the notification details passed from AdminActivity
        ArrayList<String> notificationDetails = getIntent().getStringArrayListExtra("notificationDetails");

        if (notificationDetails != null) {
            for (String notif : notificationDetails) {
                String[] details = notif.split(" for | - | in | on | at ");
                String absenceno = details[details.length - 3]; // Adjust index based on agentUid's position

                TableRow tableRow = new TableRow(this);
                tableRow.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                ));

                // Add details as TextViews
                for (String detail : details) {
                    TextView textView = new TextView(this);
                    textView.setText(detail);
                    textView.setPadding(16, 8, 16, 8);
                    textView.setTextSize(16);
                    textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                    tableRow.addView(textView);
                }

                // Add "Approve" button
                Button btnApprove = new Button(this);
                btnApprove.setText("Approve");
                btnApprove.setPadding(16, 8, 16, 8);

                // Add "Decline" button
                Button btnDecline = new Button(this);
                btnDecline.setText("Decline");
                btnDecline.setPadding(16, 8, 16, 8);

                btnApprove.setOnClickListener(v -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DatabaseReference absencesRef = FirebaseDatabase.getInstance().getReference("Absences");

                    db.collection("notification")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                boolean found = false;
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    // Get the notifications array from each document
                                    List<Map<String, Object>> notifications = (List<Map<String, Object>>) document.get("notifications");

                                    if (notifications != null) {
                                        for (Map<String, Object> notification : notifications) {
                                            if (absenceno.equals(notification.get("AbsenceNo"))) {
                                                // Update the "reclamation" field to "accepted"
                                                notification.put("reclamation", "accepted");

                                                // Update the document in Firestore
                                                db.collection("notification")
                                                        .document(document.getId())
                                                        .update("notifications", notifications)
                                                        .addOnSuccessListener(aVoid -> {
                                                            // Find and delete the item in "Absences" collection
                                                            absencesRef.get().addOnSuccessListener(dataSnapshot -> {
                                                                boolean deletionFound = false;
                                                                for (DataSnapshot absenceSnapshot : dataSnapshot.getChildren()) {
                                                                    for (DataSnapshot childSnapshot : absenceSnapshot.getChildren()) {
                                                                        Map<String, Object> absenceInfo = (Map<String, Object>) childSnapshot.getValue();
                                                                        if (absenceInfo != null && absenceno.equals(absenceInfo.get("Number"))) {
                                                                            // Remove the entire node
                                                                            childSnapshot.getRef().removeValue()
                                                                                    .addOnSuccessListener(aVoid2 -> {
                                                                                        btnApprove.setEnabled(false);
                                                                                        btnDecline.setEnabled(false);
                                                                                        db.collection("reclamations")
                                                                                                .get()
                                                                                                .addOnSuccessListener(reclamationsSnapshot -> {
                                                                                                    boolean reclamationFound = false;
                                                                                                    for (DocumentSnapshot reclamationDoc : reclamationsSnapshot.getDocuments()) {
                                                                                                        List<Map<String, Object>> reclamations = (List<Map<String, Object>>) reclamationDoc.get("reclamations");
                                                                                                        if (reclamations != null) {
                                                                                                            for (Map<String, Object> reclamation : reclamations) {
                                                                                                                if (reclamation!=null && absenceno.equals(reclamation.get("absenceno"))) {
                                                                                                                    reclamation.put("reclamation", "Accepted");

                                                                                                                    // Update the reclamation document
                                                                                                                    db.collection("reclamations")
                                                                                                                            .document(reclamationDoc.getId())
                                                                                                                            .update("reclamations", reclamations)
                                                                                                                            .addOnSuccessListener(aVoid3 -> {
                                                                                                                                // Handle success here
                                                                                                                            })
                                                                                                                            .addOnFailureListener(e -> {
                                                                                                                                // Handle failure here
                                                                                                                            });
                                                                                                                    reclamationFound = true;
                                                                                                                    break;
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        if (reclamationFound) break;
                                                                                                    }
                                                                                                })
                                                                                                .addOnFailureListener(e -> {
                                                                                                    // Handle failure to fetch "reclamations"
                                                                                                });
                                                                                    })
                                                                                    .addOnFailureListener(e -> {
                                                                                        // Handle failure to remove absence
                                                                                    });
                                                                            deletionFound = true;
                                                                            break;
                                                                        }
                                                                    }
                                                                    if (deletionFound) break; // Exit loop after deleting the record
                                                                }

                                                                if (!deletionFound) {
                                                                    // Handle absence not found
                                                                }
                                                            }).addOnFailureListener(e -> {
                                                                // Handle failure to fetch absences
                                                            });
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            // Handle failure to update notifications
                                                        });

                                                found = true; // Mark as found to avoid unnecessary looping
                                                break;
                                            }
                                        }
                                    }

                                    if (found) break; // Stop iterating once the matching document is updated
                                }

                                if (!found) {
                                    // Handle notification not found
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure to fetch notifications
                            });
                });



                btnDecline.setOnClickListener(v -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("notification")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                boolean found = false;
                                boolean deletionFound = false;

                                // Loop through each document in the "notification" collection
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    List<Map<String, Object>> notifications = (List<Map<String, Object>>) document.get("notifications");

                                    if (notifications != null) {
                                        // Loop through each notification in the notifications list
                                        for (Map<String, Object> notification : notifications) {
                                            if (absenceno.equals(notification.get("AbsenceNo"))) {
                                                // Update the "reclamation" field to "declined"
                                                notification.put("reclamation", "declined");

                                                // Update the document in Firestore
                                                db.collection("notification")
                                                        .document(document.getId())
                                                        .update("notifications", notifications)
                                                        .addOnSuccessListener(aVoid -> {
                                                            // Handle success here if needed
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            // Handle failure here if needed
                                                        });

                                                found = true; // Mark as found to stop further looping
                                                btnApprove.setEnabled(false);
                                                btnDecline.setEnabled(false);

                                                // Now update the "reclamations" collection
                                                db.collection("reclamations")
                                                        .get()
                                                        .addOnSuccessListener(reclamationsSnapshot -> {
                                                            boolean reclamationFound = false;

                                                            for (DocumentSnapshot reclamationDoc : reclamationsSnapshot.getDocuments()) {
                                                                List<Map<String, Object>> reclamations = (List<Map<String, Object>>) reclamationDoc.get("reclamations");

                                                                if (reclamations != null) {
                                                                    for (Map<String, Object> reclamation : reclamations) {
                                                                        if (reclamation != null && absenceno.equals(reclamation.get("absenceno"))) {
                                                                            reclamation.put("reclamation", "declined");

                                                                            // Update the reclamation document
                                                                            db.collection("reclamations")
                                                                                    .document(reclamationDoc.getId())
                                                                                    .update("reclamations", reclamations)
                                                                                    .addOnSuccessListener(aVoid3 -> {
                                                                                        // Handle success here if needed
                                                                                    })
                                                                                    .addOnFailureListener(e -> {
                                                                                        // Handle failure here if needed
                                                                                    });

                                                                            reclamationFound = true;
                                                                            break;
                                                                        }
                                                                    }
                                                                }

                                                                if (reclamationFound) break;
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            // Handle failure to fetch "reclamations"
                                                        });

                                                deletionFound = true;
                                                break; // Exit the loop once the record is deleted
                                            }
                                        }
                                    }

                                    if (deletionFound) break; // Exit loop after updating the "notification"
                                }

                                if (!deletionFound) {
                                    // Handle absence not found
                                }

                                if (!found) {
                                    // Handle case where notification is not found
                                }

                            })
                            .addOnFailureListener(e -> {
                                // Handle failure to fetch "notifications"
                            });
                });

                // Add buttons to the row
                tableRow.addView(btnApprove);
                tableRow.addView(btnDecline);

                // Add the TableRow to the TableLayout
                tableLayoutNotifications.addView(tableRow);
            }
        }
    }

}
