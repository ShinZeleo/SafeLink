package com.example.safelink;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.safelink.database.HistoryRepository;
import com.example.safelink.fragments.HomeFragment;
import com.example.safelink.models.HistoryModel;
import com.example.safelink.models.ScanResult;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Track last clipboard URL to avoid repeated snackbars
    private String lastClipboardUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);

        setupNavigation();

        // Feature 1: Handle incoming share intent from other apps
        handleIncomingShareIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingShareIntent(intent);
    }

    /**
     * Feature 1: Intercept ACTION_SEND intents.
     * When the user shares text/URL from another app (WhatsApp, Chrome, etc.),
     * we detect it here and pre-fill the URL field in HomeFragment.
     */
    private void handleIncomingShareIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (!TextUtils.isEmpty(sharedText)) {
                // Extract URL from shared text
                String url = extractUrl(sharedText);
                if (url != null) {
                    // Pass to HomeFragment after navigation is set up
                    android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                    handler.postDelayed(() -> sendUrlToHomeFragment(url), 300);
                }
            }
        }
    }

    /**
     * Extract a URL from text (in case user shares "Check this: https://...")
     */
    private String extractUrl(String text) {
        if (text == null) return null;
        // Direct URL check
        if (Patterns.WEB_URL.matcher(text.trim()).matches()) return text.trim();
        // Try to find URL within the text
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (Patterns.WEB_URL.matcher(word).matches()) return word;
        }
        return null;
    }

    /**
     * Sends the extracted URL to the HomeFragment's URL input field.
     * If user is not on HomeFragment, navigates there first.
     */
    private void sendUrlToHomeFragment(String url) {
        // Navigate to home tab first (in case user is on another tab)
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.homeFragment);
        }

        // Small delay to allow fragment transaction to complete
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);
            if (navHostFragment != null) {
                androidx.fragment.app.Fragment currentFragment = navHostFragment.getChildFragmentManager()
                        .getPrimaryNavigationFragment();
                if (currentFragment instanceof HomeFragment) {
                    ((HomeFragment) currentFragment).setUrlFromExternal(url);
                }
            }
        }, 150);
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }

    /**
     * Feature 2: Clipboard Monitor on every resume.
     * Shows a Snackbar if a URL is found on clipboard that hasn't been shown already.
     */
    @Override
    protected void onResume() {
        super.onResume();
        checkClipboardForUrl();
    }

    private void checkClipboardForUrl() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null || !clipboard.hasPrimaryClip()) return;

        ClipData clip = clipboard.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) return;

        CharSequence text = clip.getItemAt(0).getText();
        if (text == null) return;

        String clipText = text.toString().trim();

        // Only show if it's a URL and we haven't shown it yet
        if (Patterns.WEB_URL.matcher(clipText).matches() && !clipText.equals(lastClipboardUrl)) {
            lastClipboardUrl = clipText;
            final String urlToScan = clipText;

            // Check if it's a known dangerous URL first
            executor.execute(() -> {
                HistoryRepository repo = new HistoryRepository(this);
                HistoryModel cached = repo.findByUrl(urlToScan);
                runOnUiThread(() -> {
                    if (cached != null && ScanResult.STATUS_DANGEROUS.equalsIgnoreCase(cached.getStatus())) {
                        // Known dangerous: show alert
                        showMaliciousAlert(urlToScan);
                    } else {
                        // Unknown URL: show friendly Snackbar prompt
                        showClipboardSnackbar(urlToScan);
                    }
                });
            });
        }
    }

    private void showClipboardSnackbar(String url) {
        android.view.View rootView = findViewById(android.R.id.content);
        if (rootView == null) return;

        Snackbar.make(rootView,
                "🔗 URL terdeteksi di clipboard. Scan sekarang?",
                Snackbar.LENGTH_LONG)
                .setAction("SCAN", v -> sendUrlToHomeFragment(url))
                .setActionTextColor(getResources().getColor(R.color.accent_green, null))
                .show();
    }

    private void showMaliciousAlert(String url) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Peringatan Keamanan")
                .setMessage("Link yang baru saja Anda salin terdeteksi BERBAHAYA oleh SafeLink:\n\n" + url + "\n\nHarap berhati-hati!")
                .setPositiveButton("Mengerti", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void navigateToHistory() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.historyFragment);
        }
    }

    public void navigateToHome() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.homeFragment);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            return navHostFragment.getNavController().navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
