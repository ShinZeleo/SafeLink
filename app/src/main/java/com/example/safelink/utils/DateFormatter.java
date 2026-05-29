package com.example.safelink.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {

    private static final String STORE_FORMAT   = "yyyy-MM-dd HH:mm:ss";
    private static final String DISPLAY_FORMAT = "dd MMM yyyy, HH:mm";

    /** Returns current timestamp in storage format. */
    public static String getCurrentTimestamp() {
        return new SimpleDateFormat(STORE_FORMAT, Locale.getDefault()).format(new Date());
    }

    /** Converts a storage-format timestamp to a human-readable display format. */
    public static String formatForDisplay(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return "";
        try {
            Date date = new SimpleDateFormat(STORE_FORMAT, Locale.getDefault()).parse(timestamp);
            return new SimpleDateFormat(DISPLAY_FORMAT, new Locale("id", "ID")).format(date);
        } catch (Exception e) {
            return timestamp;
        }
    }
}
