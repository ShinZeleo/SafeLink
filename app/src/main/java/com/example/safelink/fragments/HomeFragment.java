package com.example.safelink.fragments;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safelink.R;
import com.example.safelink.activities.ResultActivity;
import com.example.safelink.adapters.HistoryAdapter;
import com.example.safelink.database.HistoryRepository;
import com.example.safelink.models.ApiResponse;
import com.example.safelink.models.HistoryModel;
import com.example.safelink.models.ScanResult;
import com.example.safelink.network.ApiClient;
import com.example.safelink.utils.DateFormatter;
import com.example.safelink.utils.NetworkHelper;
import com.example.safelink.utils.ValidationHelper;
import com.google.gson.Gson;
import androidx.activity.result.ActivityResultLauncher;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.airbnb.lottie.LottieAnimationView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TextInputLayout tilUrl;
    private TextInputEditText etUrl;
    private Button btnScan;
    private View btnPaste, btnInfo;
    private LottieAnimationView lottieLoading;
    private TextView tvTotalScans, tvSafeCount, tvDangerCount, tvDailyTip, tvTimeline;
    private View tvEmptyRecent;
    private RecyclerView rvRecent;
    private HistoryRepository repo;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    etUrl.setText(result.getContents());
                    Toast.makeText(requireContext(), "QR Code Scan Berhasil", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Scan dibatalkan", Toast.LENGTH_SHORT).show();
                }
            });
    private HistoryAdapter recentAdapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilUrl = view.findViewById(R.id.til_url);
        etUrl = view.findViewById(R.id.et_url);
        btnScan = view.findViewById(R.id.btn_scan);
        btnPaste = view.findViewById(R.id.btn_paste);
        View btnScanQr = view.findViewById(R.id.btn_scan_qr);
        btnInfo = view.findViewById(R.id.btn_info);
        lottieLoading = view.findViewById(R.id.lottie_loading);
        tvTotalScans = view.findViewById(R.id.tv_total_scans);
        tvSafeCount = view.findViewById(R.id.tv_safe_count);
        tvDangerCount = view.findViewById(R.id.tv_danger_count);
        tvDailyTip = view.findViewById(R.id.tv_daily_tip);
        tvTimeline = view.findViewById(R.id.tv_timeline);
        tvEmptyRecent = view.findViewById(R.id.tv_empty_recent);
        rvRecent = view.findViewById(R.id.rv_recent);

        repo = new HistoryRepository(requireContext());

        setupRecentRecyclerView();
        setupListeners(btnScanQr, view.findViewById(R.id.btn_see_all));
        loadDailyTip();
        
        // UX Polish: Entrance animation
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(500).start();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupListeners(View btnScanQr, View btnSeeAll) {
        etUrl.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    String url = ValidationHelper.normalizeUrl(s.toString());
                    if (!ValidationHelper.isValidUrl(url)) {
                        tilUrl.setError(getString(R.string.error_invalid_url));
                    } else {
                        tilUrl.setError(null);
                        tilUrl.setHelperText("URL Valid");
                    }
                } else {
                    tilUrl.setError(null);
                    tilUrl.setHelperText(null);
                }
            }
        });

        btnPaste.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData clip = clipboard.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0) {
                    CharSequence text = clip.getItemAt(0).getText();
                    if (text != null) {
                        etUrl.setText(text);
                        Toast.makeText(requireContext(), "URL di-paste", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        View.OnTouchListener touchListener = (v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.button_press));
                    break;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.button_release));
                    break;
                case MotionEvent.ACTION_CANCEL:
                    v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.button_release));
                    break;
            }
            return false;
        };
        btnScan.setOnTouchListener(touchListener);

        btnScan.setOnClickListener(v -> onScanClicked());

        btnInfo.setOnClickListener(v -> showInfoDialog());

        if (btnScanQr != null) {
            btnScanQr.setOnClickListener(v -> {
                ScanOptions options = new ScanOptions();
                options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
                options.setPrompt("Scan QR Code Link");
                options.setCameraId(0);
                options.setBeepEnabled(true);
                options.setBarcodeImageEnabled(true);
                options.setOrientationLocked(true); // Lock it completely
                options.setCaptureActivity(com.example.safelink.activities.PortraitCaptureActivity.class);
                barcodeLauncher.launch(options);
            });
        }

        if (btnSeeAll != null) {
            btnSeeAll.setOnClickListener(v -> {
                // Navigate to History Fragment
                if (getActivity() instanceof com.example.safelink.MainActivity) {
                    ((com.example.safelink.MainActivity) getActivity()).navigateToHistory();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStats();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cancel any pending runnables posted to views (e.g. setUrlFromExternal)
        if (etUrl != null) ((View) etUrl).removeCallbacks(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }

    /**
     * Feature 1: Called by MainActivity when app is opened via Share intent or clipboard snackbar.
     * Pre-fills the URL input field and optionally triggers the scan.
     */
    public void setUrlFromExternal(String url) {
        if (etUrl != null && url != null) {
            etUrl.setText(url);
            etUrl.setSelection(url.length());
            // Auto-trigger scan after a short delay for smooth UX
            etUrl.postDelayed(this::onScanClicked, 200);
        }
    }

    private void onScanClicked() {
        String rawUrl = etUrl.getText() != null ? etUrl.getText().toString().trim() : "";

        if (rawUrl.isEmpty()) {
            tilUrl.setError(getString(R.string.error_empty_url));
            return;
        }

        String url = ValidationHelper.normalizeUrl(rawUrl);
        if (!ValidationHelper.isValidUrl(url)) {
            tilUrl.setError(getString(R.string.error_invalid_url));
            return;
        }

        // Performance: Check Cache
        checkCacheAndScan(url);
    }

    private void checkCacheAndScan(String url) {
        setLoading(true);
        executor.execute(() -> {
            // Layer 1: Local Heuristic
            if (com.example.safelink.utils.UrlHeuristicEngine.isSuspicious(url)) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        setLoading(false);
                        com.google.android.material.snackbar.Snackbar.make(requireView(), "Potentially suspicious pattern detected (Offline Scan)", com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                                .setBackgroundTint(getResources().getColor(R.color.accent_orange, null))
                                .setTextColor(getResources().getColor(R.color.primary_on, null))
                                .show();
                        ScanResult result = new ScanResult();
                        result.setUrl(url);
                        result.setStatus(ScanResult.STATUS_SUSPICIOUS);
                        result.setRiskLevel("Sedang");
                        result.setRecommendation("Pola URL mencurigakan terdeteksi secara offline.");
                        result.setScannedAt(DateFormatter.getCurrentTimestamp());
                        // Optional: save to cache
                        openResult(result);
                    });
                }
                return;
            }

            // Layer 2: SQLite Cache Check
            HistoryModel cached = repo.findByUrl(url);
            if (cached != null) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(requireContext(), "Mengambil hasil dari cache...", Toast.LENGTH_SHORT).show();
                        openResultFromHistory(cached);
                    });
                }
            } else {
                // Layer 3: Online API Scan
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (!NetworkHelper.isConnected(requireContext())) {
                            setLoading(false);
                            Toast.makeText(requireContext(), getString(R.string.error_no_internet), Toast.LENGTH_LONG).show();
                            return;
                        }
                        performScan(url);
                    });
                }
            }
        });
    }

    private void performScan(String url) {
        // VirusTotal v3 requires base64url encoded URL without padding
        String urlId = android.util.Base64.encodeToString(url.getBytes(), android.util.Base64.URL_SAFE | android.util.Base64.NO_PADDING | android.util.Base64.NO_WRAP);
        
        Call<ApiResponse> call = ApiClient.getService().checkUrl(urlId);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResp = response.body();
                    ScanResult result = ScanResult.fromApiResponse(apiResp, url);
                    result.setScannedAt(DateFormatter.getCurrentTimestamp());
                    
                    // Save to SQLite Cache
                    executor.execute(() -> {
                        HistoryModel history = new HistoryModel();
                        history.setUrl(result.getUrl());
                        history.setStatus(result.getStatus());
                        history.setRiskLevel(result.getRiskLevel());
                        history.setRecommendation(result.getRecommendation());
                        history.setScannedAt(result.getScannedAt());
                        repo.insert(history);
                    });

                    openResult(result, apiResp);
                } else {
                    String errorText;
                    if (response.code() == 401 || response.code() == 403) errorText = "API Key tidak valid atau Limit API tercapai.";
                    else if (response.code() == 404) errorText = "URL belum pernah di-scan di VirusTotal (Not Found).";
                    else if (response.code() == 429) errorText = "Terlalu banyak request. Silakan tunggu.";
                    else errorText = getString(R.string.error_api);
                    Toast.makeText(requireContext(), errorText, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                setLoading(false);
                String msg = t.getMessage() != null && t.getMessage().contains("timeout")
                        ? getString(R.string.error_timeout)
                        : getString(R.string.error_api);
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openResult(ScanResult result, ApiResponse apiResponse) {
        Intent intent = new Intent(requireContext(), ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_URL, result.getUrl());
        intent.putExtra(ResultActivity.EXTRA_STATUS, result.getStatus());
        intent.putExtra(ResultActivity.EXTRA_RISK_LEVEL, result.getRiskLevel());
        intent.putExtra(ResultActivity.EXTRA_RECOMMENDATION, result.getRecommendation());
        intent.putExtra(ResultActivity.EXTRA_SCANNED_AT, result.getScannedAt());
        // Feature 3: Pass ApiResponse as JSON for detailed engine display
        if (apiResponse != null) {
            intent.putExtra(ResultActivity.EXTRA_API_RESPONSE_JSON, new Gson().toJson(apiResponse));
        }
        startActivity(intent);
    }

    private void openResult(ScanResult result) {
        openResult(result, null);
    }

    private void openResultFromHistory(HistoryModel item) {
        Intent intent = new Intent(requireContext(), ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_URL, item.getUrl());
        intent.putExtra(ResultActivity.EXTRA_STATUS, item.getStatus());
        intent.putExtra(ResultActivity.EXTRA_RISK_LEVEL, item.getRiskLevel());
        intent.putExtra(ResultActivity.EXTRA_RECOMMENDATION, item.getRecommendation());
        intent.putExtra(ResultActivity.EXTRA_SCANNED_AT, item.getScannedAt());
        startActivity(intent);
    }

    private void setLoading(boolean loading) {
        btnScan.setEnabled(!loading);
        btnScan.setAlpha(loading ? 0.6f : 1f);
        if (loading) {
            lottieLoading.setVisibility(View.VISIBLE);
            lottieLoading.playAnimation();
        } else {
            lottieLoading.cancelAnimation();
            lottieLoading.setVisibility(View.GONE);
        }
    }

    private void setupRecentRecyclerView() {
        recentAdapter = new HistoryAdapter(null);
        rvRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecent.setAdapter(recentAdapter);
        rvRecent.setNestedScrollingEnabled(false);
    }

    private void refreshStats() {
        executor.execute(() -> {
            List<HistoryModel> recent = repo.getRecent(3);
            int total = repo.countAll();
            int safe = repo.countByStatus(ScanResult.STATUS_SAFE);
            int danger = repo.countByStatus(ScanResult.STATUS_DANGEROUS);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvTotalScans.setText(String.valueOf(total));
                    tvSafeCount.setText(String.valueOf(safe));
                    tvDangerCount.setText(String.valueOf(danger));

                    // Today's Activity Timeline (simulate for today)
                    String timelineText = "- " + danger + " suspicious links blocked\n- " + safe + " safe sites verified";
                    tvTimeline.setText(timelineText);

                    if (recent.isEmpty()) {
                        rvRecent.setVisibility(View.GONE);
                        tvEmptyRecent.setVisibility(View.VISIBLE);
                    } else {
                        rvRecent.setVisibility(View.VISIBLE);
                        tvEmptyRecent.setVisibility(View.GONE);
                        recentAdapter.updateData(recent);
                    }
                });
            }
        });
    }

    private void showInfoDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_about, null);
        
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("OK", null)
                .show();
    }
    
    private void loadDailyTip() {
        executor.execute(() -> {
            try {
                // Use BufferedReader for reliable full-file reading
                java.io.InputStream is = requireContext().getAssets().open("tips.json");
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                String json = sb.toString();
                org.json.JSONArray array = new org.json.JSONArray(json);
                if (array.length() == 0) return;
                int randomIdx = (int) (Math.random() * array.length());
                org.json.JSONObject tip = array.getJSONObject(randomIdx);
                String tipText = tip.optString("title", "") + ": " + tip.optString("shortDesc", "");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (tvDailyTip != null) tvDailyTip.setText(tipText);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
