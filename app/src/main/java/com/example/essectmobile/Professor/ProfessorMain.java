package com.example.essectmobile.Professor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.AppCompatActivity;

import com.example.essectmobile.R;
import com.example.essectmobile.Firebase.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Map;

public class ProfessorMain extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TableLayout tblNotifications;

    // ViewModel instance
    private ProfessorMainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor);

        mAuth = FirebaseAuth.getInstance();
        tblNotifications = findViewById(R.id.tblNotifications);

        // Initialize ViewModel
        viewModel = new ProfessorMainViewModel();

        // Observe LiveData from ViewModel
        viewModel.getNotifications().observe(this, notifications -> {
            if (notifications != null && !notifications.isEmpty()) {
                displayNotifications(notifications);
            } else {
                Toast.makeText(ProfessorMain.this, "No notifications found", Toast.LENGTH_SHORT).show();
            }
        });

        // Save FCM token
        saveProfessorFcmToken();

        // Fetch Notifications from ViewModel
        fetchNotifications();
    }

    private void fetchNotifications() {
        String uid = mAuth.getCurrentUser().getUid();
        viewModel.fetchNotifications(uid);
    }

    private void displayNotifications(List<Map<String, Object>> notifications) {
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

            String reclamation = (String) notification.get("reclamation");

            TableRow row = new TableRow(ProfessorMain.this);
            row.addView(createTextView(AbsenceNo));
            row.addView(createTextView(currentDate));
            row.addView(createTextView(group));
            row.addView(createTextView(professorName));
            row.addView(createTextView(room));
            row.addView(createTextView(subject));
            row.addView(createTextView(timeSlot));

            Button reclamationButton = new Button(ProfessorMain.this);
            if (reclamation == null || reclamation.isEmpty()) {
                reclamationButton.setText("Reclamation");
                reclamationButton.setEnabled(true);
                reclamationButton.setOnClickListener(v -> openReclamationForm(notification));
            } else {
                reclamationButton.setText(reclamation);
                reclamationButton.setEnabled(false);
            }
            row.addView(reclamationButton);
            tblNotifications.addView(row);
        }
    }

    private void openReclamationForm(Map<String, Object> notification) {
        Intent intent = new Intent(this, ProfessorReclamation.class);
        intent.putExtra("AbsenceNo", (String) notification.get("AbsenceNo"));
        intent.putExtra("professorName", (String) notification.get("professorName"));
        intent.putExtra("subject", (String) notification.get("subject"));
        intent.putExtra("group", (String) notification.get("group"));
        intent.putExtra("timeSlot", (String) notification.get("timeSlot"));
        intent.putExtra("agentUid", (String) notification.get("agent uid"));

        startActivity(intent);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 8, 16, 8);
        textView.setTextSize(16);
        return textView;
    }

    private void saveProfessorFcmToken() {
        String uid = mAuth.getCurrentUser().getUid();

        FirebaseHelper.saveProfessorFcmToken(uid, new FirebaseHelper.SaveTokenCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(ProfessorMain.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ProfessorMain.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
