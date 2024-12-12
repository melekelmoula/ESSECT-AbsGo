package com.example.essectmobile.Admin;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.essectmobile.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;

public class AdminAbsencesList extends AppCompatActivity {

    private TableLayout tableLayoutAbsences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absence_details);

        tableLayoutAbsences = findViewById(R.id.tableLayoutAbsences);

        // Get the absence details passed from AdminActivity
        ArrayList<String> absenceDetails = getIntent().getStringArrayListExtra("absenceDetails");

        if (absenceDetails != null) {
            // Iterate through the absence details and create table rows
            for (String absenceDetail : absenceDetails) {
                // Split the absence details (e.g., professorName for group - subject on date at timeSlot)
                String[] details = absenceDetail.split(" for | - | in | AgentID | on | at ");

                // Extract the agentID from the last element in the split array
                String agentID = details[details.length - 1]; // Since agentID is the last element

                // Remove the agentID from the details array
                String[] detailsWithoutAgentID = Arrays.copyOfRange(details, 0, details.length - 1);

                // Create a new TableRow for each absence
                TableRow tableRow = new TableRow(this);
                tableRow.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                ));

                for (String detail : detailsWithoutAgentID) {
                    TextView textView = new TextView(this);
                    textView.setText(detail);
                    textView.setPadding(16, 8, 16, 8);
                    textView.setTextSize(16);
                    textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

                    tableRow.addView(textView);
                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                        .whereEqualTo("uid", agentID) // Query using agentID
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                String agentName = document.getString("name");

                                TextView textView = new TextView(this);
                                textView.setText(agentName);
                                textView.setPadding(16, 8, 16, 8);
                                textView.setTextSize(16);
                                textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

                                tableRow.addView(textView);
                            }
                        });


                // Add the TableRow to the TableLayout
                tableLayoutAbsences.addView(tableRow);
            }
        }

    }
}
