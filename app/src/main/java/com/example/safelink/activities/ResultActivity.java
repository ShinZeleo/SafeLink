package com.example.safelink.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safelink.R;
import com.example.safelink.adapters.EngineResultAdapter;
import com.example.safelink.database.BookmarkRepository;
import com.example.safelink.database.HistoryRepository;
import com.example.safelink.models.ApiResponse;
import com.example.safelink.models.BookmarkModel;
import com.example.safelink.models.HistoryModel;
import com.example.safelink.models.ScanResult;
import com.example.safelink.utils.SecurityScoreEngine;
import com.example.safelink.utils.ValidationHelper;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_STATUS = "extra_status";
    public static final String EXTRA_RISK_LEVEL = "extra_risk_level";
    public static final String EXTRA_RECOMMENDATION = "extra_recommendation";
    public static final String EXTRA_SCANNED_AT = "extra_scanned_at";
    public static final String EXTRA_API_RESPONSE_JSON = "extra_api_response_json";

    private String url, status, risk, recommendation, scannedAt;
    private ApiResponse apiResponse;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean engineListExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_result);

        url = getIntent().getStringExtra(EXTRA_URL);
        status = getIntent().getStringExtra(EXTRA_STATUS);
        risk = getIntent().getStringExtra(EXTRA_RISK_LEVEL);
        recommendation = getIntent().getStringExtra(EXTRA_RECOMMENDATION);
        scannedAt = getIntent().getStringExtra(EXTRA_SCANNED_AT);

        // Feature 3: Deserialize ApiResponse from JSON extra
        String apiJson = getIntent().getStringExtra(EXTRA_API_RESPONSE_JSON);
        if (apiJson != null) {
            try {
                apiResponse = new Gson().fromJson(apiJson, ApiResponse.class);
            } catch (Exception ignored) {}
        }

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

        tvStatus.setText(status != null ? status.toUpperCase() : "UNKNOWN");
        tvRisk.setText("Risiko: " + (risk != null ? risk : "N/A"));
        tvUrl.setText(url != null ? url : "-");
        tvRec.setText(recommendation != null ? recommendation : "-");
        tvTime.setText("Diperiksa pada " + (scannedAt != null ? scannedAt : "-"));

        boolean isHttps = url != null && url.toLowerCase().startsWith("https");
        int score = SecurityScoreEngine.calculateScore(status, risk, isHttps, null, false);
        tvScore.setText(score + "/100");

        // Status styling
        if (ScanResult.STATUS_SAFE.equalsIgnoreCase(status)) {
            tvStatus.setText("TRUSTED");
            tvStatus.setTextColor(getResources().getColor(R.color.accent_green, null));
            findViewById(R.id.fl_status_circle).setBackgroundResource(R.drawable.bg_status_safe);
            ((TextView) findViewById(R.id.tv_status_icon)).setText("✓");
            ((TextView) findViewById(R.id.tv_status_icon)).setTextColor(getResources().getColor(R.color.accent_green, null));
        } else if (ScanResult.STATUS_DANGEROUS.equalsIgnoreCase(status)) {
            tvStatus.setText("DANGEROUS");
            tvStatus.setTextColor(getResources().getColor(R.color.accent_red, null));
            findViewById(R.id.fl_status_circle).setBackgroundResource(R.drawable.bg_status_dangerous);
            ((TextView) findViewById(R.id.tv_status_icon)).setText("!");
            ((TextView) findViewById(R.id.tv_status_icon)).setTextColor(getResources().getColor(R.color.accent_red, null));
        } else if (ScanResult.STATUS_SUSPICIOUS.equalsIgnoreCase(status)) {
            tvStatus.setText("SUSPICIOUS");  // Bug fix: was missing setText for suspicious
            tvStatus.setTextColor(getResources().getColor(R.color.accent_orange, null));
            findViewById(R.id.fl_status_circle).setBackgroundResource(R.drawable.bg_status_suspicious);
            ((TextView) findViewById(R.id.tv_status_icon)).setText("?");
            ((TextView) findViewById(R.id.tv_status_icon)).setTextColor(getResources().getColor(R.color.accent_orange, null));
        }

        // Feature 3: Show engine analysis card if ApiResponse data is available
        setupEngineAnalysisCard();

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
                String shareText = "SafeLink Scan Result:\nURL: " + url
                        + "\nStatus: " + (status != null ? status.toUpperCase() : "UNKNOWN")
                        + "\nRisk: " + risk;
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(shareIntent, "Share Result"));
            });
        }

        btnOpen.setOnClickListener(v -> {
            try {
                String normalized = ValidationHelper.normalizeUrl(url);
                if (ScanResult.STATUS_DANGEROUS.equalsIgnoreCase(status)) {
                    Toast.makeText(this, "Link diblokir karena BERBAHAYA!", Toast.LENGTH_LONG).show();
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

    /**
     * Feature 3: Show per-engine stats chips and collapsible engine result list.
     */
    private void setupEngineAnalysisCard() {
        View cardEngine = findViewById(R.id.card_engine_stats);
        if (apiResponse == null || cardEngine == null) return;

        ApiResponse.Attributes attrs = apiResponse.getData() != null
                ? apiResponse.getData().getAttributes() : null;
        if (attrs == null || attrs.getLastAnalysisStats() == null) return;

        ApiResponse.LastAnalysisStats stats = attrs.getLastAnalysisStats();
        List<ApiResponse.EngineResultItem> engineItems = apiResponse.getEngineResults();
        if (engineItems.isEmpty()) return;

        cardEngine.setVisibility(View.VISIBLE);

        ((TextView) findViewById(R.id.tv_stat_malicious)).setText(String.valueOf(stats.getMalicious()));
        ((TextView) findViewById(R.id.tv_stat_suspicious)).setText(String.valueOf(stats.getSuspicious()));
        ((TextView) findViewById(R.id.tv_stat_harmless)).setText(String.valueOf(stats.getHarmless()));

        RecyclerView rv = findViewById(R.id.rv_engine_results);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new EngineResultAdapter(engineItems));

        TextView tvToggle = findViewById(R.id.tv_engine_toggle);
        tvToggle.setOnClickListener(v -> {
            engineListExpanded = !engineListExpanded;
            rv.setVisibility(engineListExpanded ? View.VISIBLE : View.GONE);
            tvToggle.setText(engineListExpanded ? "Sembunyikan ▲" : "Tampilkan ▼");
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
