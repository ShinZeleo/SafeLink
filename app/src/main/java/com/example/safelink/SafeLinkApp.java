package com.example.safelink;

import android.app.Application;
import com.example.safelink.utils.ThemeHelper;
import com.google.android.material.color.DynamicColors;

public class SafeLinkApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Material 3 Dynamic Colors – adapts to wallpaper on Android 12+
        DynamicColors.applyToActivitiesIfAvailable(this);

        try {
            System.loadLibrary("sqlcipher");
        } catch (Throwable t) {
            android.util.Log.e("SafeLinkApp", "Failed to load SQLCipher library: " + t.getMessage(), t);
        }
    }
}
