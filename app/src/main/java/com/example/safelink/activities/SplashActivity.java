package com.example.safelink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.example.safelink.MainActivity;
import com.example.safelink.R;
import com.example.safelink.utils.ThemeHelper;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+ uses overrideActivityTransition (non-deprecated)
                overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                        android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                //noinspection deprecation
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            startActivity(intent);
            finish();
        }, SPLASH_DELAY_MS);
    }
}

