package com.example.safelink;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.safelink.database.HistoryRepository;
import com.example.safelink.models.HistoryModel;
import com.example.safelink.models.ScanResult;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNavigation();
        setupClipboardMonitor();
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

    private void setupClipboardMonitor() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.addPrimaryClipChangedListener(() -> {
                if (clipboard.hasPrimaryClip()) {
                    ClipData clip = clipboard.getPrimaryClip();
                    if (clip != null && clip.getItemCount() > 0) {
                        CharSequence text = clip.getItemAt(0).getText();
                        if (text != null && text.toString().startsWith("http")) {
                            checkMaliciousClipboard(text.toString());
                        }
                    }
                }
            });
        }
    }

    private void checkMaliciousClipboard(String url) {
        executor.execute(() -> {
            HistoryRepository repo = new HistoryRepository(this);
            HistoryModel cached = repo.findByUrl(url);
            if (cached != null && ScanResult.STATUS_DANGEROUS.equalsIgnoreCase(cached.getStatus())) {
                runOnUiThread(() -> showMaliciousAlert(url));
            }
        });
    }

    private void showMaliciousAlert(String url) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Peringatan Keamanan")
                .setMessage("Link yang baru saja Anda salin terdeteksi BERBAHAYA oleh SafeLink:\n\n" + url + "\n\nHarap berhati-hati!")
                .setPositiveButton("Mengerti", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
