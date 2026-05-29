package com.example.safelink.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.safelink.R;
import com.example.safelink.database.BookmarkRepository;
import com.example.safelink.database.HistoryRepository;
import com.example.safelink.models.BookmarkModel;
import com.example.safelink.models.HistoryModel;
import com.example.safelink.models.ScanResult;
import com.example.safelink.utils.SecurityScoreEngine;
import com.example.safelink.utils.ValidationHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_STATUS = "extra_status";
    public static final String EXTRA_RISK_LEVEL = "extra_risk_level";
    public static final String EXTRA_RECOMMENDATION = "extra_recommendation";
    public static final String EXTRA_SCANNED_AT = "extra_scanned_at";

    private String url, status, risk, recommendation, scannedAt;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Get Data
        url = getIntent().getStringExtra(EXTRA_URL);
        status = getIntent().getStringExtra(EXTRA_STATUS);
        risk = getIntent().getStringExtra(EXTRA_RISK_LEVEL);
        recommendation = getIntent().getStringExtra(EXTRA_RECOMMENDATION);
        scannedAt = getIntent().getStringExtra(EXTRA_SCANNED_AT);

        setupUI();
    }

    private void setupUI() {
        TextView tvStatus = findViewById(R.id.tv_status_text);
        TextView tvRisk = findViewById(R.id.tv_risk_level);
        TextView tvUrl = findViewById(R.id.tv_scanned_url);
        TextView tvRec = findViewById(R.id.tv_recommendation);
        TextView tvTime = findViewById(R.id.tv_timestamp);
        TextView tvScore = findViewById(R.id.tv_security_score);
        
        View btnBack = findViewById(R.id.btn_back);
        TextView btnCopy = findViewById(R.id.btn_copy_url);
        Button btnSave = findViewById(R.id.btn_save_history);
        Button btnBookmark = findViewById(R.id.btn_bookmark);
        Button btnOpen = findViewById(R.id.btn_open_browser);
        View btnScanAnother = findViewById(R.id.btn_scan_another);

        // Set Values
        tvStatus.setText(status != null ? status.toUpperCase() : "UNKNOWN");
        tvRisk.setText("Risiko: " + (risk != null ? risk : "N/A"));
        tvUrl.setText(url);
        tvRec.setText(recommendation);
        tvTime.setText("Diperiksa pada " + scannedAt);
        
        // Use realistic score calculation
        boolean isHttps = url != null && url.toLowerCase().startsWith("https");
        int score = SecurityScoreEngine.calculateScore(status, risk, isHttps, null, false);
        tvScore.setText(score + "/100");

        // Styling based on status
        if (ScanResult.STATUS_SAFE.equalsIgnoreCase(status)) {
            tvStatus.setText("TRUSTED");
            tvStatus.setTextColor(getResources().getColor(R.color.accent_green, null));
            findViewById(R.id.fl_status_circle).setBackgroundResource(R.drawable.bg_status_safe);
            ((TextView)findViewById(R.id.tv_status_icon)).setText("✓");
            ((TextView)findViewById(R.id.tv_status_icon)).setTextColor(getResources().getColor(R.color.accent_green, null));
        } else if (ScanResult.STATUS_DANGEROUS.equalsIgnoreCase(status)) {
            tvStatus.setText("DANGEROUS");
            tvStatus.setTextColor(getResources().getColor(R.color.accent_red, null));
            findViewById(R.id.fl_status_circle).setBackgroundResource(R.drawable.bg_status_dangerous);
            ((TextView)findViewById(R.id.tv_status_icon)).setText("!");
            ((TextView)findViewById(R.id.tv_status_icon)).setTextColor(getResources().getColor(R.color.accent_red, null));
        } else {
            tvStatus.setText(status != null ? status.toUpperCase() : "UNKNOWN");
            if (ScanResult.STATUS_SUSPICIOUS.equalsIgnoreCase(status)) {
                tvStatus.setTextColor(getResources().getColor(R.color.accent_orange, null));
                findViewById(R.id.fl_status_circle).setBackgroundResource(R.drawable.bg_status_suspicious);
                ((TextView)findViewById(R.id.tv_status_icon)).setText("?");
                ((TextView)findViewById(R.id.tv_status_icon)).setTextColor(getResources().getColor(R.color.accent_orange, null));
            }
        }

        // Listeners
        btnBack.setOnClickListener(v -> finish());
        btnScanAnother.setOnClickListener(v -> finish());

        btnCopy.setOnClickListener(v -> {
            ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cb != null) {
                cb.setPrimaryClip(ClipData.newPlainText("URL", url));
                Toast.makeText(this, "URL disalin", Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(v -> saveToHistory());
        btnBookmark.setOnClickListener(v -> saveToBookmark());
        
        View btnShare = findViewById(R.id.btn_share);
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareText = "SafeLink Scan Result:\nURL: " + url + "\nStatus: " + (status != null ? status.toUpperCase() : "UNKNOWN") + "\nRisk: " + risk;
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(shareIntent, "Share Result"));
            });
        }

        btnOpen.setOnClickListener(v -> {
            try {
                String normalized = ValidationHelper.normalizeUrl(url);
                if (ScanResult.STATUS_DANGEROUS.equalsIgnoreCase(status)) {
                    Toast.makeText(this, "Link is blocked because it is DANGEROUS", Toast.LENGTH_LONG).show();
                } else {
                    Intent i = new Intent(this, SafeBrowserActivity.class);
                    i.putExtra("extra_url", normalized);
                    startActivity(i);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Tidak bisa membuka browser", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToHistory() {
        executor.execute(() -> {
            HistoryRepository repo = new HistoryRepository(this);
            HistoryModel model = new HistoryModel(0, url, status, risk, recommendation, scannedAt);
            repo.insert(model);
            runOnUiThread(() -> Toast.makeText(this, "Berhasil disimpan ke riwayat", Toast.LENGTH_SHORT).show());
        });
    }

    private void saveToBookmark() {
        executor.execute(() -> {
            BookmarkRepository repo = new BookmarkRepository(this);
            BookmarkModel model = new BookmarkModel();
            model.setUrl(url);
            model.setScannedAt(scannedAt);
            model.setStatus(status);
            model.setTitle("Bookmarked URL");
            repo.insert(model);
            runOnUiThread(() -> Toast.makeText(this, "Berhasil disimpan ke bookmark", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
