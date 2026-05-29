package com.example.safelink.database;

import android.content.Context;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 3; // Bumped to 3
    public static final String DB_PASSWORD = "safelink_secret_key_2026";

    public DatabaseHelper(Context context) {
        super(context, DatabaseContract.DB_NAME, null, DB_VERSION);
        try {
            SQLiteDatabase.loadLibs(context);
        } catch (Throwable t) {
            android.util.Log.e("DatabaseHelper", "Failed to load SQLCipher libs: " + t.getMessage(), t);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.HistoryEntry.CREATE_TABLE);
        db.execSQL(DatabaseContract.BookmarkEntry.CREATE_TABLE);
        db.execSQL(DatabaseContract.TrustedDomainEntry.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(DatabaseContract.BookmarkEntry.CREATE_TABLE);
        }
        if (oldVersion < 3) {
            db.execSQL(DatabaseContract.TrustedDomainEntry.CREATE_TABLE);
        }
    }
}
