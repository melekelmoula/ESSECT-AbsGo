package com.example.essectmobile.Admin;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Collections;

import com.example.essectmobile.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import java.text.ParseException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class AdminMain extends AppCompatActivity {
    private Button btnUploadExcel, btnAddUser;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText etNewEmail, etNewPassword, etProfessorName;
    private Spinner spinnerRole;
    private Button btnSeeAbsences;
    private Button btnSeeNotifs;
    private Button btnSeeReclams;
    private Button btnSeeDash;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        btnSeeAbsences = findViewById(R.id.btnSeeAbsence); // Reference to the new button
        btnSeeNotifs= findViewById(R.id.btnSeeNotif);
        btnSeeReclams= findViewById(R.id.btnSeeReclam);
        btnSeeDash= findViewById(R.id.btnDashboard);

        // Handle See Absences button click
        btnSeeAbsences.setOnClickListener(v -> {
            fetchAbsenceDetails();
        });

        btnSeeNotifs.setOnClickListener(v -> {
            fetchNotifDetails();
        });

        btnSeeReclams.setOnClickListener(v -> {
            fetchReclamDetails();
        });

        btnSeeDash.setOnClickListener(v -> openDashboard());


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        btnUploadExcel = findViewById(R.id.btnUploadExcel);
        btnAddUser = findViewById(R.id.btnAddUser);
        etNewEmail = findViewById(R.id.etNewEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etProfessorName = findViewById(R.id.etProfessorName);  // Professor name input
        spinnerRole = findViewById(R.id.spinnerRole);
        btnUploadExcel.setOnClickListener(v -> openFilePicker());
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_roles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        btnAddUser.setOnClickListener(v -> addNewUser());
    }

    private void openDashboard() {
        Intent intent = new Intent(AdminMain.this, AdminDashboard.class);
        startActivity(intent);
    }

    private void fetchReclamDetails() {
        db.collection("reclamations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        ArrayList<String> reclamationsDetails = new ArrayList<>(); // Ensure this list is declared

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            List<Map<String, Object>> reclamations =
                                    (List<Map<String, Object>>) document.get("reclamations");

                            if (reclamations != null) {
                                for (Map<String, Object> reclamation : reclamations) {
                                    if (reclamation.get("reclamation") == null) {
                                        String agentUid = (String) reclamation.get("agentUid");
                                        String group = (String) reclamation.get("group");
                                        String professorName = (String) reclamation.get("professorName");
                                        String reclamationText = (String) reclamation.get("reclamationText");
                                        String subject = (String) reclamation.get("subject");
                                        String timeSlot = (String) reclamation.get("timeSlot");
                                        String number = (String) reclamation.get("absenceno");
                                        String date = (String) reclamation.get("date");

                                        // Format notification details
                                        String detail = String.format(
                                                "%s for %s - %s in %s on %s at %s at %s at %s",
                                                professorName != null ? professorName : "Unknown",
                                                group != null ? group : "Unknown",
                                                subject != null ? subject : "Unknown",
                                                agentUid != null ? agentUid : "Unknown",
                                                timeSlot != null ? timeSlot : "Unknown",
                                                number != null ? number : "Unknown",
                                                date != null ? date : "Unknown",
                                                reclamationText != null ? reclamationText : "Unknown"
                                        );

                                        reclamationsDetails.add(detail);
                                    }
                                }
                            }
                        }

                        // Navigate to NotificationDetailsActivity if notifications exist
                        if (!reclamationsDetails.isEmpty()) {
                            Intent intent = new Intent(AdminMain.this, AdminReclamationsList.class);
                            intent.putStringArrayListExtra("notificationDetails", reclamationsDetails);
                            startActivity(intent);
                        } else {
                            Toast.makeText(AdminMain.this, "No reclamations records found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void fetchNotifDetails() {
        db.collection("notification")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        ArrayList<String> notificationsDetails = new ArrayList<>(); // Ensure this list is declared

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            List<Map<String, Object>> notifications =
                                    (List<Map<String, Object>>) document.get("notifications");
                            if (notifications != null) {
                                for (Map<String, Object> notification : notifications) {
                                    String agentUid = (String) notification.get("agent uid");
                                    String currentDate = (String) notification.get("currentDate");
                                    String group = (String) notification.get("group");
                                    String professorName = (String) notification.get("professorName");
                                    String room = (String) notification.get("room");
                                    String subject = (String) notification.get("subject");
                                    String timeSlot = (String) notification.get("timeSlot");

                                    // Format notification details
                                    String detail = String.format(
                                            "%s for %s - %s in %s on %s at %s",
                                            professorName != null ? professorName : "Unknown",
                                            group != null ? group : "Unknown",
                                            subject != null ? subject : "Unknown",
                                            room != null ? room : "Unknown",
                                            currentDate != null ? currentDate : "Unknown",
                                            timeSlot != null ? timeSlot : "Unknown"
                                    );

                                    notificationsDetails.add(detail);
                                }
                            }
                        }

                        Collections.sort(notificationsDetails, (a, b) -> {
                            try {
                                // Extract the professor name from the formatted string
                                String professorNameA = a.split(" for ")[0];
                                String professorNameB = b.split(" for ")[0];

                                // Compare professor names alphabetically
                                return professorNameA.compareTo(professorNameB);
                            } catch (Exception e) {
                                Log.e("AbsenceDetails", "Error in sorting by professor name", e);
                            }
                            return 0;
                        });

                        // Navigate to NotificationDetailsActivity if notifications exist
                        if (!notificationsDetails.isEmpty()) {
                            Intent intent = new Intent(AdminMain.this, AdminNotificationsList.class);
                            intent.putStringArrayListExtra("notificationDetails", notificationsDetails);
                            startActivity(intent);
                        } else {
                            Toast.makeText(AdminMain.this, "No notification records found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    private void fetchAbsenceDetails() {
        DatabaseReference absencesRef = FirebaseDatabase.getInstance().getReference("Absences");

        absencesRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                ArrayList<String> absences = new ArrayList<>();

                // Iterate over each absence entry by unique ID (e.g., Ih286sBJrYZL9VQdC6U4Sp05qYN2)
                for (DataSnapshot absenceSnapshot : dataSnapshot.getChildren()) {
                    // For each unique Absence ID, get its child nodes (e.g., -ODgQwYadN0xeawtxT4M)
                    for (DataSnapshot childSnapshot : absenceSnapshot.getChildren()) {
                        Map<String, Object> absenceInfo = (Map<String, Object>) childSnapshot.getValue();
                        if (absenceInfo != null) {
                            String absenceDate = (String) absenceInfo.get("day");
                            String absenceTimeSlot = (String) absenceInfo.get("timesession");
                            String group = (String) absenceInfo.get("group");
                            String subject = (String) absenceInfo.get("subject");
                            String professorName = (String) absenceInfo.get("professorName");
                            String room = (String) absenceInfo.get("room");
                            String agentuid = (String) absenceInfo.get("agentId");
                            String absenceno = (String) absenceInfo.get("Number");

                            absenceno = absenceno != null ? absenceno : "No date available";
                            absenceDate = absenceDate != null ? absenceDate : "No date available";
                            absenceTimeSlot = absenceTimeSlot != null ? absenceTimeSlot : "No time slot available";
                            group = group != null ? group : "No group available";
                            subject = subject != null ? subject : "No subject available";
                            professorName = professorName != null ? professorName : "No professor available";
                            room = room != null ? room : "No room available";  // Ensure room is not null
                            agentuid = agentuid != null ? agentuid : "No agent available";  // Ensure agentId is not null


                            absences.add(absenceDate + " for " + absenceno + " for " + professorName + " on " + group + " - " + subject + " at " + absenceTimeSlot + " in " + room + " AgentID " + agentuid );

                        }
                    }
                }

                // Sort the absences list based on the date (assuming absenceDate is in a recognizable format like yyyy-MM-dd)
                Collections.sort(absences, (a, b) -> {
                    try {
                        // Extract the date string from the formatted absence detail
                        String dateA = a.split(" ")[0];
                        String dateB = b.split(" ")[0];

                        // Parse the dates
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date dA = dateFormat.parse(dateA);
                        Date dB = dateFormat.parse(dateB);

                        if (dA != null && dB != null) {
                            return dA.compareTo(dB);
                        }
                    } catch (ParseException e) {
                        Log.e("AbsenceDetails", "Date parsing error", e);
                    }
                    return 0;
                });

                // Check if we have any absence details to display
                if (!absences.isEmpty()) {
                    Intent intent = new Intent(AdminMain.this, AdminAbsencesList.class);
                    intent.putStringArrayListExtra("absenceDetails", absences);
                    startActivity(intent);
                } else {
                    Toast.makeText(AdminMain.this, "No absence records found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AdminMain.this, "No absences found in the database", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("AbsenceDetails", "Failed to fetch absence details", e);
            Toast.makeText(AdminMain.this, "Failed to fetch absence details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }



    private void addNewUser() {
        String email = etNewEmail.getText().toString().trim();
        String password = etNewPassword.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString(); // Get selected role
        String professorName = etProfessorName.getText().toString().trim(); // Get professor name if available

        if (email.isEmpty() || password.isEmpty() || professorName.isEmpty()) {
            Toast.makeText(AdminMain.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the new user directly without checking if the email already exists
        mAuth.createUserWithEmailAndPassword(email, password)

                .addOnCompleteListener(this, authTask -> {
                    if (authTask.isSuccessful()) {
                        String uid = authTask.getResult().getUser().getUid();
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("role", role); // Set role from Spinner selection
                        userData.put("pw", password); // Set password
                        userData.put("uid", uid); // Set password
                        if ("Professor".equals(role) ) {
                            userData.put("professorName", professorName);  // Store professor's name
                        }
                        else {
                            userData.put("name", professorName);  // Store professor's name
                        }
                        db.collection("users").document(uid)
                                .set(userData)
                                .addOnCompleteListener(firestoreTask -> {
                                    if (firestoreTask.isSuccessful()) {
                                        Toast.makeText(AdminMain.this, "User added successfully!", Toast.LENGTH_SHORT).show();
                                        mAuth.signOut(); //signout from newly created
                                    } else {
                                        Toast.makeText(AdminMain.this, "Firestore Error: " + firestoreTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                });
        resetFields();
    }


    private void resetFields() {
        // Clear all fields when the role changes
        etNewEmail.setText("");
        etNewPassword.setText("");
        etProfessorName.setText(""); // Reset professor's name
        etNewEmail.requestFocus();

    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                processExcelFile(fileUri);
            }
        }
    }

    private void processExcelFile(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // Assuming the first sheet

            // List to store all tables' schedules
            ArrayList<Map<String, Map<String, ArrayList<Map<String, String>>>>> allTables = new ArrayList<>();

            Map<String, Map<String, ArrayList<Map<String, String>>>> currentTable = null;
            Row headerRow = null;
            String tableName = null;

            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                String firstCellValue = row.getCell(0) != null ? row.getCell(0).toString() : "";

                // Check for table name (starting with "*")
                if (firstCellValue.startsWith("*")) {
                    if (currentTable != null && !currentTable.isEmpty()) {
                        allTables.add(currentTable); // Save previous table before starting a new one
                    }

                    tableName = firstCellValue.substring(1); // Remove "*" from the table name
                    currentTable = new HashMap<>();
                    headerRow = row;
                    initializeDays(headerRow, currentTable);
                } else if (headerRow != null && currentTable != null) {
                    processRow(row, headerRow, currentTable, tableName);
                }
            }

            // Adding the last table after loop completes
            if (currentTable != null && !currentTable.isEmpty()) {
                allTables.add(currentTable);
            }

            Log.d("AllTables", "Tables: " + allTables.toString());

            // Merge all tables into a single map
            Map<String, Map<String, ArrayList<Map<String, String>>>> mergedTables = new HashMap<>();
            for (Map<String, Map<String, ArrayList<Map<String, String>>>> table : allTables) {
                for (Map.Entry<String, Map<String, ArrayList<Map<String, String>>>> entry : table.entrySet()) {
                    String day = entry.getKey();
                    Map<String, ArrayList<Map<String, String>>> times = entry.getValue();
                    mergedTables.putIfAbsent(day, new HashMap<>());
                    Map<String, ArrayList<Map<String, String>>> existingTimes = mergedTables.get(day);

                    for (Map.Entry<String, ArrayList<Map<String, String>>> timeEntry : times.entrySet()) {
                        String time = timeEntry.getKey();
                        existingTimes.putIfAbsent(time, new ArrayList<>());
                        existingTimes.get(time).addAll(timeEntry.getValue());
                    }
                }
            }

            // Upload merged data to Firestore
            uploadDataToFirestore(mergedTables);

            workbook.close();
        } catch (Exception e) {
            Toast.makeText(AdminMain.this, "Error processing file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadDataToFirestore(Map<String, Map<String, ArrayList<Map<String, String>>>> mergedSchedule) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date minDate = null;
            Date maxDate = null;

            for (String dateStr : mergedSchedule.keySet()) {
                // Check if the string length is sufficient before extracting the date
                if (dateStr.length() >= 10) {
                    // Extract the last 10 characters (dd/MM/yyyy format)
                    String extractedDate = dateStr.substring(dateStr.length() - 10);
                    Date date = dateFormat.parse(extractedDate);

                    if (minDate == null || date.before(minDate)) {
                        minDate = date;
                    }
                    if (maxDate == null || date.after(maxDate)) {
                        maxDate = date;
                    }
                } else {
                    Log.e("Firestore Error", "Invalid date format: " + dateStr);
                }
            }

            if (minDate != null && maxDate != null) {
                // Format dates for the document ID in dd-MM-yyyy format
                SimpleDateFormat idFormat = new SimpleDateFormat("dd-MM-yyyy");
                String minDateStr = idFormat.format(minDate);
                String maxDateStr = idFormat.format(maxDate);

                String documentId = minDateStr + "_to_" + maxDateStr;

                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("schedule", mergedSchedule);

                db.collection("emploi")
                        .document(documentId)
                        .set(dataMap)
                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "Upload Successful"))
                        .addOnFailureListener(e -> Log.e("Firestore Error", e.getMessage()));
            } else {
                Toast.makeText(this, "No valid dates found in the schedule!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("Firestore Error", "Error calculating date range: " + e.getMessage());
            Toast.makeText(this, "Error processing date range!", Toast.LENGTH_SHORT).show();
        }
    }


    private void initializeDays(Row headerRow, Map<String, Map<String, ArrayList<Map<String, String>>>> table) {
        for (int i = 1; i < headerRow.getPhysicalNumberOfCells(); i++) {
            String day = headerRow.getCell(i).toString().trim(); // Use full "Day Date"
            table.put(day, new HashMap<>());
        }
    }

    private void processRow(Row row, Row headerRow, Map<String, Map<String, ArrayList<Map<String, String>>>> table, String tableName) {
        String timeInterval = row.getCell(0) != null ? row.getCell(0).toString() : "";

        for (int i = 1; i < row.getPhysicalNumberOfCells(); i++) {
            String session = row.getCell(i) != null ? row.getCell(i).toString() : "";
            String day = headerRow.getCell(i).toString().trim();

            if (!session.isEmpty() && table.containsKey(day)) {
                Map<String, String> sessionData = new HashMap<>();
                sessionData.put("session", session);
                sessionData.put("Group", tableName); // Add the table name to the session data

                // Group by time for each day
                table.get(day).putIfAbsent(timeInterval, new ArrayList<>());
                table.get(day).get(timeInterval).add(sessionData);
            }
        }
    }
}
