package com.example.essectmobile.Admin;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.essectmobile.R;

import java.util.ArrayList;

public class AdminNotificationsList extends AppCompatActivity {

    private TableLayout tableLayoutNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifs_details);

        tableLayoutNotifications = findViewById(R.id.tableLayoutNotifications);

        // Get the notification details passed from AdminActivity
        ArrayList<String> notificationDetails = getIntent().getStringArrayListExtra("notificationDetails");

        if (notificationDetails != null) {
            // Iterate through the notification details and create table rows
            for (String notif : notificationDetails) {
                String[] details = notif.split(" for | - | in | on | at ");
                TableRow tableRow = new TableRow(this);
                tableRow.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                ));

                for (String detail : details) {
                    TextView textView = new TextView(this);
                    textView.setText(detail);
                    textView.setPadding(16, 8, 16, 8);
                    textView.setTextSize(16);
                    textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

                    tableRow.addView(textView);
                }

                // Add the TableRow to the TableLayout
                tableLayoutNotifications.addView(tableRow);
            }
        }
    }
}
