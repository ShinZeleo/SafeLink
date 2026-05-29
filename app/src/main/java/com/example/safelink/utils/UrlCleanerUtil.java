package com.example.safelink.utils;

import android.net.Uri;

public class UrlCleanerUtil {
    
    public static String cleanUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isEmpty()) return rawUrl;
        
        try {
            Uri uri = Uri.parse(ValidationHelper.normalizeUrl(rawUrl));
            String cleaned = uri.getScheme() + "://" + uri.getHost() + uri.getPath();
            // Optional: retain some safe query params, but we strip all for simplicity
            return cleaned;
        } catch (Exception e) {
            return rawUrl;
        }
    }
}
