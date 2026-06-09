package com.example.safelink.database;

import android.content.Context;
import net.zetetic.database.sqlcipher.SQLiteDatabase;
import net.zetetic.database.sqlcipher.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 3; // Bumped to 3
    public static final String DB_PASSWORD = "safelink_secret_key_2026";

    public DatabaseHelper(Context context) {
        super(context, DatabaseContract.DB_NAME, DB_PASSWORD, null, DB_VERSION, 0, null, null, false);
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
