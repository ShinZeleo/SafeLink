package com.example.safelink.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

public class NetworkHelper {

    /**
     * Returns true if the device has an active internet connection.
     * Uses modern NetworkCapabilities API (Android 10+) with fallback for older devices.
     */
    @SuppressWarnings("deprecation")
    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network activeNetwork = cm.getActiveNetwork();
            if (activeNetwork == null) return false;
            NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
            return caps != null && (
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        } else {
            // Fallback for API < 23
            android.net.NetworkInfo activeInfo = cm.getActiveNetworkInfo();
            return activeInfo != null && activeInfo.isConnectedOrConnecting();
        }
    }
}
