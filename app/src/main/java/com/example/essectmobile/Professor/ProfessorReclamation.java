package com.example.essectmobile.Professor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.essectmobile.R;
import com.example.essectmobile.Firebase.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfessorReclamation extends AppCompatActivity {

    private EditText etReclamationText;
    private Button btnSubmitReclamation;
    private String professorName;
    private String subject;
    private String group;
    private String timeSlot;
    private String agentUid;
    private String absenceno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reclamation);

        etReclamationText = findViewById(R.id.etReclamationText);
        btnSubmitReclamation = findViewById(R.id.btnSubmitReclamation);

        professorName = getIntent().getStringExtra("professorName");
        subject = getIntent().getStringExtra("subject");
        group = getIntent().getStringExtra("group");
        timeSlot = getIntent().getStringExtra("timeSlot");
        agentUid = getIntent().getStringExtra("agentUid");
        absenceno = getIntent().getStringExtra("AbsenceNo");

        TextView tvNotificationDetails = findViewById(R.id.tvNotificationDetails);
        tvNotificationDetails.setText(absenceno + " Professor: " + professorName + "\nSubject: " + subject +
                "\nGroup: " + group + "\nTime Slot: " + timeSlot);

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

        Map<String, Object> reclamation = new HashMap<>();
        reclamation.put("reclamationText", reclamationText);
        reclamation.put("professorName", professorName);
        reclamation.put("subject", subject);
        reclamation.put("group", group);
        reclamation.put("timeSlot", timeSlot);
        reclamation.put("agentUid", agentUid);
        reclamation.put("date", currentDate);
        reclamation.put("absenceno", absenceno);

        String reclamationId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseHelper.submitReclamation(reclamationId, reclamation, new FirebaseHelper.ReclamationCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(ProfessorReclamation.this, message, Toast.LENGTH_SHORT).show();
                updateNotification();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ProfessorReclamation.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNotification() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseHelper.updateNotificationReclamation(userId, absenceno, new FirebaseHelper.NotificationUpdateCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(ProfessorReclamation.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ProfessorReclamation.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
