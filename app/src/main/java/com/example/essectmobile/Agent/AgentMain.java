package com.example.essectmobile.Agent;
import android.view.MotionEvent;

import com.example.essectmobile.R;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;  // <-- Add this import
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashMap;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;

public class AgentMain extends AppCompatActivity {

    private TableLayout tableLayout;
    private FirebaseFirestore db;
    private String currentDateStr;
    private String currentTimeStr;
    private Spinner timeSlotSpinner;
    private TreeMap<String, List<Map<String, String>>> fullSchedule;
    private Map<String, List<Map<String, Object>>> absenceCache = new HashMap<>(); // Cache for absence details
    private int absenceDetailsLength = 0;
    private boolean isSpinnerProgrammaticallySet = false; // Flag to prevent infinite loop


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent);

        tableLayout = findViewById(R.id.scheduleTable);
        db = FirebaseFirestore.getInstance();
        timeSlotSpinner = findViewById(R.id.timeSlotSpinner);

        fetchScheduleForCurrentDate();

        timeSlotSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Prevent the infinite loop if the spinner selection is set programmatically
                if (isSpinnerProgrammaticallySet) {
                    isSpinnerProgrammaticallySet = false;
                    return;
                }

                String selectedTime = (String) timeSlotSpinner.getSelectedItem();
                if (selectedTime != null) {
                    // If "All" is selected, show the full schedule
                    if (selectedTime.equals("Select Time")) {
                        displayScheduleInTable(fullSchedule);
                    } else {
                        // Filter schedule based on the selected time
                        Map<String, List<Map<String, String>>> filteredSchedule = new TreeMap<>();
                        if (fullSchedule.containsKey(selectedTime)) {
                            filteredSchedule.put(selectedTime, fullSchedule.get(selectedTime));
                        }
                        displayScheduleInTable(filteredSchedule);
                    }

                    // Set the spinner's value to the selected time (this ensures that the displayed value matches the selection)
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) timeSlotSpinner.getAdapter();
                    int selectedIndex = adapter.getPosition(selectedTime);

                    // Flag to indicate that the selection is being set programmatically
                    isSpinnerProgrammaticallySet = true;
                    timeSlotSpinner.setSelection(selectedIndex);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        Button resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(v -> resetSchedule());

    }

    private void displayScheduleInTable(Map<String, List<Map<String, String>>> schedule) {
        tableLayout.removeAllViews();
        fullSchedule = new TreeMap<>(schedule); // Store full schedule for filtering

        // Populate Spinner with time slots
        List<String> timeSlots = new ArrayList<>(schedule.keySet());
        timeSlots.add(0, "Select Time");  // Add "All" at the start of the list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeSlots);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSlotSpinner.setAdapter(adapter);

        // Headers
        TableRow headerRow = new TableRow(this);
        TextView timeHeader = createStyledTextView("Time", true);
        TextView groupHeader = createStyledTextView("Group", true);
        TextView professorHeader = createStyledTextView("Professor", true);
        TextView TotalabHeader = createStyledTextView("TotalAbsence", true);
        TextView subjectHeader = createStyledTextView("Subject", true);
        TextView roomHeader = createStyledTextView("Room", true);
        TextView absentHeader = createStyledTextView("Absent", true); // Absent header

        headerRow.addView(timeHeader);
        headerRow.addView(groupHeader);
        headerRow.addView(professorHeader);
        headerRow.addView(TotalabHeader);
        headerRow.addView(subjectHeader);
        headerRow.addView(roomHeader);
        headerRow.addView(absentHeader); // Add absent button header
        tableLayout.addView(headerRow);

        // Chronological order using TreeMap
        TreeMap<String, List<Map<String, String>>> sortedSchedule = new TreeMap<>(schedule);
        for (Map.Entry<String, List<Map<String, String>>> entry : sortedSchedule.entrySet()) {
            String timeSlot = entry.getKey();
            List<Map<String, String>> sessions = entry.getValue();

            for (Map<String, String> session : sessions) {
                TableRow tableRow = new TableRow(this);

                TextView timeTextView = createStyledTextView(timeSlot, false);
                TextView groupTextView = createStyledTextView(session.get("Group"), false);

                String[] sessionDetails = session.get("session").split(" - ");
                String professor = sessionDetails.length > 0 ? sessionDetails[0] : "";
                String subject = sessionDetails.length > 1 ? sessionDetails[1] : "";
                String room = sessionDetails.length > 2 ? sessionDetails[2] : "";

                TextView professorTextView = createStyledTextView(professor, false);
                TextView subjectTextView = createStyledTextView(subject, false);
                TextView roomTextView = createStyledTextView(room, false);
                TextView totalabTextView = createStyledTextView("0", false);

                Button absentButton = new Button(this);
                absentButton.setText("Absent");

                absentButton.setEnabled(true);

                checkAndUpdateButtons(professor, timeSlot, absentButton, totalabTextView);
                tableRow.addView(timeTextView);
                tableRow.addView(groupTextView);
                tableRow.addView(professorTextView);
                tableRow.addView(totalabTextView);
                tableRow.addView(subjectTextView);
                tableRow.addView(roomTextView);
                tableRow.addView(absentButton);

                tableLayout.addView(tableRow);
                tableRow.setClickable(true);
                tableRow.setFocusable(true);
                tableRow.setFocusableInTouchMode(true);

                absentButton.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        // Create an AlertDialog with Confirm and Cancel buttons
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Confirm Absence");

                        builder.setMessage("Group: " + session.get("Group") + "\n" +
                                "Time: " + timeSlot + "\n" +
                                "Professor: " + professor + "\n" +
                                "Subject: " + subject + "\n" +
                                "Room: " + room);

                        builder.setPositiveButton("Confirm", (dialog, which) -> {
                            absentButton.setEnabled(false);
                            totalabTextView.setText(String.valueOf(Integer.parseInt(totalabTextView.getText().toString()) + 1));
                            updateUserScheduleOnAbsence(professor, timeSlot, room, subject , session.get("Group"));
                            dialog.dismiss();
                        });

                        builder.setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();  // Explicitly dismiss the dialog after canceling
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                    return true;  // Return true to indicate that the event was handled
                });
            }
        }
    }

    private void checkAndUpdateButtons(String professor, String timeSlot, Button absentButton, TextView totalabTextView) {

        // Fetch users where the professor's name matches
        db.collection("users")
                .whereEqualTo("professorName", professor)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Assuming the documents have a field with the professor's UID
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            String professorUid = doc.getId();  // Fetch the professor UID from the document ID
                            // Fetch absences from the Realtime Database for the given professor's UID
                            DatabaseReference absencesRef = FirebaseDatabase.getInstance().getReference("Absences").child(professorUid);
                            absencesRef.get().addOnSuccessListener(dataSnapshot -> {
                                if (dataSnapshot.exists()) {
                                    // Iterate over all absences and check for matching time slot
                                    for (DataSnapshot absenceSnapshot : dataSnapshot.getChildren()) {
                                        Map<String, Object> absenceInfo = (Map<String, Object>) absenceSnapshot.getValue();
                                        if (absenceInfo != null) {
                                            String absenceDate = (String) absenceInfo.get("day");
                                            String absenceTimeSlot = (String) absenceInfo.get("timesession");

                                            // Log the fetched absence data
                                            Log.d("Firebase", "Checking absence data: " + absenceDate + " " + absenceTimeSlot);
                                            Log.d("Firebase", "Current timeSlot: " + timeSlot);

                                            // Check if the absence matches today's date and the given time slot
                                            if (absenceDate.equals(getCurrentDate()) && absenceTimeSlot.equals(timeSlot)) {
                                                    absentButton.setEnabled(false);
                                            }
                                            totalabTextView.setText(String.valueOf(dataSnapshot.getChildrenCount()));

                                        }
                                    }
                                } else {
                                    totalabTextView.setText("0");
                                }
                            }).addOnFailureListener(e -> {
                                Log.e("Firebase", "Error fetching absences: " + e.getMessage());
                            });
                        }
                    }
                }).addOnFailureListener(e -> {
                    Log.e("Firebase", "Error fetching professor UID: " + e.getMessage());
                });
    }


    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }


    private void updateUserScheduleOnAbsence(String professor, String timeSlot, String room, String subject, String group) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(AgentMain.this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String useruid = user.getUid();

        // Get a reference to the Realtime Database
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        // Query the professor to get the professor's UID (if needed for FCM or other references)
        db.collection("users")
                .whereEqualTo("professorName", professor)  // Find the user with the professor's name
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            String professorUid = documentSnapshot.getId(); // Get professor's UID
                            int randomN = new Random().nextInt(1000000); // Generate a number between 0 and 999999
                            String randomNumber = String.valueOf(randomN);

                            // Create the absence entry data
                            Map<String, Object> absenceEntry = new HashMap<>();
                            absenceEntry.put("professorName", professor);
                            absenceEntry.put("timesession", timeSlot);
                            absenceEntry.put("room", room);
                            absenceEntry.put("group", group);
                            absenceEntry.put("subject", subject);
                            absenceEntry.put("Number", randomNumber);
                            absenceEntry.put("day", currentDate);
                            absenceEntry.put("agentId", useruid);  // Store the agent's UID who reported the absence

                            // Get the current list of absences for this professor
                            database.child("Absences")
                                    .child(professorUid)  // Key: professor UID
                                    .get()
                                    .addOnSuccessListener(dataSnapshot -> {
                                        List<Map<String, Object>> absenceList = new ArrayList<>();
                                        if (dataSnapshot.exists()) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                absenceList.add((Map<String, Object>) snapshot.getValue());
                                            }
                                        }
                                        // Add the new absence entry to the list
                                        absenceList.add(absenceEntry);

                                        // Update the list of absences under the professor's UID
                                        database.child("Absences")
                                                .child(professorUid)
                                                .setValue(absenceList)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("Absence Update", "Absence details added for professor: " + professor);
                                                    Toast.makeText(AgentMain.this, "Absence recorded for " + professor, Toast.LENGTH_SHORT).show();

                                                    // Optionally send FCM notification to professor
                                                    sendAbsenceNotification(professor, timeSlot, room, subject, group, currentDate, useruid, professorUid, randomNumber);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Absence Update", "Error updating absence details: " + e.getMessage());
                                                    Toast.makeText(AgentMain.this, "Error recording absence", Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Absence Update", "Error fetching absences: " + e.getMessage());
                                        Toast.makeText(AgentMain.this, "Error fetching current absences", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // If no user with the professor's name is found, show a message
                        Toast.makeText(AgentMain.this, "No professor found with name: " + professor, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("User Fetch Error", "Error fetching user data: " + e.getMessage());
                    Toast.makeText(AgentMain.this, "Error fetching professor details", Toast.LENGTH_SHORT).show();
                });
    }




    private void sendAbsenceNotification(String professor, String timeSlot, String room, String subject, String group, String currentDate, String uid, String profuid,String absenceno) {
        db.collection("users")
                .whereEqualTo("professorName", professor)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            String fcmToken = documentSnapshot.getString("fcmToken");
                            String notificationMessage = "Dear " + professor + ", You have been marked as absent for the session at "
                                    + timeSlot + " in " + room + " for subject " + subject + " with " + group + " on " + currentDate + ". Agent: " + uid;

                            if (fcmToken != null && !fcmToken.isEmpty()) {

                                // Send FCM notification
                                Map<String, String> notificationData = new HashMap<>();
                                notificationData.put("title", "Absence Recorded");
                                notificationData.put("message", notificationMessage);
                                sendFCMNotification(fcmToken, notificationData);

                                // Prepare the notification details
                                Map<String, Object> notificationDetails = new HashMap<>();
                                notificationDetails.put("professorName", professor);
                                notificationDetails.put("timeSlot", timeSlot);
                                notificationDetails.put("room", room);
                                notificationDetails.put("subject", subject);
                                notificationDetails.put("group", group);
                                notificationDetails.put("currentDate", currentDate);
                                notificationDetails.put("agent uid", uid);
                                notificationDetails.put("Notif Text", notificationMessage);
                                notificationDetails.put("AbsenceNo", absenceno);
                                notificationDetails.put("fcmToken", fcmToken);

                                // Add the notification to the "notification" collection under profuid document
                                db.collection("notification")
                                        .document(profuid) // Document ID = profuid
                                        .get() // Check if the document exists
                                        .addOnSuccessListener(documentSnapshot1 -> {
                                            if (documentSnapshot1.exists()) {
                                                // Document exists, update the notifications array
                                                db.collection("notification")
                                                        .document(profuid)
                                                        .update("notifications", FieldValue.arrayUnion(notificationDetails)) // Add notification to the array
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d("Notification Saved", "Notification added to array for professor: " + profuid);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e("Notification Save Error", "Error updating notification: " + e.getMessage());
                                                        });
                                            } else {
                                                // Document does not exist, create it with the notifications array
                                                List<Map<String, Object>> notificationsList = new ArrayList<>();
                                                notificationsList.add(notificationDetails); // Add first notification

                                                db.collection("notification")
                                                        .document(profuid)
                                                        .set(new HashMap<String, Object>() {{
                                                            put("notifications", notificationsList); // Set notifications as an array
                                                        }})
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d("Notification Saved", "Document created and notification saved for professor: " + profuid);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e("Notification Save Error", "Error saving notification: " + e.getMessage());
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(e -> Log.e("Document Fetch Error", "Error fetching document: " + e.getMessage()));
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("FCM Error", "Error fetching FCM token: " + e.getMessage()));
    }



    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/essectmobile/messages:send";
    private static final String SERVICE_ACCOUNT_JSON = "service_account_key.json";

    private void sendFCMNotification(String fcmToken, Map<String, String> notificationData) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                // Get access token on a background thread
                String accessToken = getAccessToken();
                if (accessToken == null) {
                    Log.e("FCM Error", "Error: No access token found");
                    return;
                }

                Log.d("FCM Debug", "Access Token: " + accessToken); // Log the Access Token

                // Correct the message structure
                JSONObject messageBody = new JSONObject();
                JSONObject notification = new JSONObject();
                notification.put("title", notificationData.get("title"));
                notification.put("body", notificationData.get("message"));

                JSONObject message = new JSONObject();
                message.put("token", fcmToken);
                message.put("notification", notification);

                messageBody.put("message", message);  // Corrected

                URL url = new URL(FCM_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = messageBody.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("FCM Success", "Notification sent successfully");
                } else {
                    Log.e("FCM Error", "Error sending notification: " + connection.getResponseMessage());
                }
            } catch (IOException | JSONException e) {
                Log.e("FCM Error", "IOException sending FCM message: " + e.getMessage());
            } finally {
                // Shutdown the executor service to release resources
                executorService.shutdown();
            }
        });
    }

    private String getAccessToken() {
        try {
            InputStream serviceAccountStream = getResources().openRawResource(R.raw.service_account_key);
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream)
                    .createScoped("https://www.googleapis.com/auth/firebase.messaging");

            credentials.refreshIfExpired();  // Ensure the token is valid
            AccessToken token = credentials.getAccessToken();
            String tokenValue = token.getTokenValue();

            Log.d("FCM Debug", "Access Token retrieved: " + tokenValue);  // Log the access token
            return tokenValue;
        } catch (IOException e) {
            Log.e("FCM Error", "Failed to retrieve access token: " + e.getMessage());
            return null;
        }
    }


    private void resetSchedule() {
        // Clear the table
        tableLayout.removeAllViews();

        // Reset the spinner to the default value ("All")
        timeSlotSpinner.setSelection(0);

        // Optionally, you could also fetch and display the full schedule again
        fetchScheduleForCurrentDate(); // This will load the default schedule data
    }

    private void fetchScheduleForCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String currentDateStr = dateFormat.format(new Date());

        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE dd/MM/yyyy", Locale.FRENCH);
        String currentDayStr = dayOfWeekFormat.format(new Date());
        String result = currentDayStr.substring(0, 1).toUpperCase() + currentDayStr.substring(1);

        db.collection("emploi")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        boolean documentFound = false;

                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            String docId = documentSnapshot.getId();
                            String[] dateRange = docId.split("_to_");

                            if (dateRange.length == 2) {
                                try {
                                    SimpleDateFormat docDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                    Date startDate = docDateFormat.parse(dateRange[0]);
                                    Date endDate = docDateFormat.parse(dateRange[1]);
                                    Date currentDate = dateFormat.parse(currentDateStr);

                                    if (currentDate != null && startDate != null && endDate != null &&
                                            (currentDate.equals(startDate) || currentDate.after(startDate)) &&
                                            (currentDate.equals(endDate) || currentDate.before(endDate))) {



                                    Map<String, Object> scheduleData = (Map<String, Object>) documentSnapshot.get("schedule");
                                        if (scheduleData != null) {
                                            if (scheduleData.containsKey(result)) {
                                                // Log the schedule for the current day
                                                Log.d("Schedule Data", "Schedule for today (" + result + "): " + scheduleData.get(result));
                                                displayScheduleInTable((Map<String, List<Map<String, String>>>) scheduleData.get(result));
                                            } else {
                                                Log.d("Schedule Data", "No schedule found for today (" + result + ")");
                                            }
                                        } else {
                                            Log.d("Schedule Data", "No schedule data found in document.");
                                        }
                                        documentFound = true;
                                        break;
                                    }
                                } catch (Exception e) {
                                    Log.e("Date Parsing Error", "Error parsing date: " + e.getMessage());
                                }
                            }
                        }
                    }
                });
    }

    private TextView createStyledTextView(String text, boolean isHeader) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(isHeader ? 18 : 16);
        textView.setPadding(16, 16, 16, 16);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setTypeface(null, isHeader ? Typeface.BOLD : Typeface.NORMAL);
        textView.setBackgroundResource(isHeader ? android.R.color.holo_blue_light : android.R.color.transparent);
        return textView;
    }
}