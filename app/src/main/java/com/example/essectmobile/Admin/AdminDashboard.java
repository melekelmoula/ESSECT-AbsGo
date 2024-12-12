package com.example.essectmobile.Admin;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.essectmobile.R;

public class AdminDashboard extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://app.powerbi.com/view?r=eyJrIjoiZDg1MDc5MTMtZWRhMy00ZmIyLWI3OWItNTMyMjFhNDlkOWFlIiwidCI6ImRiZDY2NjRkLTRlYjktNDZlYi05OWQ4LTVjNDNiYTE1M2M2MSIsImMiOjl9");
    }
}
