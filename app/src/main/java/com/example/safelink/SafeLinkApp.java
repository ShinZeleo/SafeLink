package com.example.safelink;

import android.app.Application;
import com.example.safelink.utils.ThemeHelper;

public class SafeLinkApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            net.sqlcipher.database.SQLiteDatabase.loadLibs(this);
        } catch (Throwable t) {
            android.util.Log.e("SafeLinkApp", "Failed to load SQLCipher libs: " + t.getMessage(), t);
        }
    }
}
