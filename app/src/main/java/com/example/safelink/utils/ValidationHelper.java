package com.example.safelink.utils;

import android.util.Patterns;
import android.text.TextUtils;

public class ValidationHelper {

    /**
     * Returns true if the URL string is non-empty and matches Android's WEB_URL pattern.
     */
    public static boolean isValidUrl(String url) {
        if (TextUtils.isEmpty(url)) return false;
        String normalized = normalizeUrl(url.trim());
        return Patterns.WEB_URL.matcher(normalized).matches();
    }

    /**
     * Prepends "https://" if the URL has no scheme.
     */
    public static String normalizeUrl(String url) {
        if (url == null) return "";
        url = url.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }
}
