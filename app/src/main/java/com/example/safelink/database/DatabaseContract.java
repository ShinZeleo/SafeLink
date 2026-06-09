package com.example.safelink.database;

public class DatabaseContract {

    public static final String DB_NAME    = "safelink.db";
    public static final int    DB_VERSION = 1;

    // Prevent instantiation
    private DatabaseContract() {}

    public static final class HistoryEntry {
        public static final String TABLE_NAME      = "history";
        public static final String COL_ID          = "id";
        public static final String COL_URL         = "url";
        public static final String COL_STATUS      = "status";
        public static final String COL_RISK_LEVEL  = "risk_level";
        public static final String COL_RECOMMEND   = "recommendation";
        public static final String COL_SCANNED_AT  = "scanned_at";
        public static final String COL_API_RESPONSE = "api_response_json";

        public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
            COL_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_URL        + " TEXT NOT NULL, " +
            COL_STATUS     + " TEXT NOT NULL, " +
            COL_RISK_LEVEL + " TEXT, " +
            COL_RECOMMEND  + " TEXT, " +
            COL_SCANNED_AT + " TEXT NOT NULL, " +
            COL_API_RESPONSE + " TEXT" +
            ")";
    }

    public static final class BookmarkEntry {
        public static final String TABLE_NAME      = "bookmarks";
        public static final String COL_ID          = "id";
        public static final String COL_URL         = "url";
        public static final String COL_TITLE       = "title";
        public static final String COL_CATEGORY    = "category";
        public static final String COL_NOTES       = "notes";
        public static final String COL_FAVICON     = "favicon";
        public static final String COL_STATUS      = "status";
        public static final String COL_SCANNED_AT  = "scanned_at";
        public static final String COL_API_RESPONSE = "api_response_json";

        public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
            COL_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_URL        + " TEXT NOT NULL, " +
            COL_TITLE      + " TEXT, " +
            COL_CATEGORY   + " TEXT, " +
            COL_NOTES      + " TEXT, " +
            COL_FAVICON    + " TEXT, " +
            COL_STATUS     + " TEXT, " +
            COL_SCANNED_AT + " TEXT, " +
            COL_API_RESPONSE + " TEXT" +
            ")";
    }

    public static final class TrustedDomainEntry {
        public static final String TABLE_NAME = "trusted_domains";
        public static final String COL_ID = "id";
        public static final String COL_DOMAIN = "domain";
        public static final String COL_ADDED_AT = "added_at";

        public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
            COL_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_DOMAIN   + " TEXT NOT NULL UNIQUE, " +
            COL_ADDED_AT + " TEXT NOT NULL" +
            ")";
    }

    public static final class BlacklistEntry {
        public static final String TABLE_NAME = "blacklist_domains";
        public static final String COL_ID = "id";
        public static final String COL_DOMAIN = "domain";
        public static final String COL_ADDED_AT = "added_at";

        public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
            COL_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_DOMAIN   + " TEXT NOT NULL UNIQUE, " +
            COL_ADDED_AT + " TEXT NOT NULL" +
            ")";
    }
}
