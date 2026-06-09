package com.example.safelink.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import net.zetetic.database.sqlcipher.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import com.example.safelink.utils.DateFormatter;

public class BlacklistRepository {
    private final DatabaseHelper dbHelper;

    public BlacklistRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long addBlacklistDomain(String domain) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.BlacklistEntry.COL_DOMAIN, domain.toLowerCase());
        values.put(DatabaseContract.BlacklistEntry.COL_ADDED_AT, DateFormatter.getCurrentTimestamp());
        long id = -1;
        try {
            id = db.insertWithOnConflict(DatabaseContract.BlacklistEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        } finally {
            db.close();
        }
        return id;
    }

    public boolean isBlacklisted(String domain) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseContract.BlacklistEntry.COL_DOMAIN + " = ?";
        String[] selectionArgs = { domain.toLowerCase() };
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.query(
                    DatabaseContract.BlacklistEntry.TABLE_NAME,
                    new String[]{DatabaseContract.BlacklistEntry.COL_ID},
                    selection,
                    selectionArgs,
                    null, null, null
            );
            if (cursor != null && cursor.getCount() > 0) {
                exists = true;
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return exists;
    }

    public List<String> getAllBlacklistedDomains() {
        List<String> domains = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    DatabaseContract.BlacklistEntry.TABLE_NAME,
                    new String[]{DatabaseContract.BlacklistEntry.COL_DOMAIN},
                    null, null, null, null, null
            );
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idx = cursor.getColumnIndex(DatabaseContract.BlacklistEntry.COL_DOMAIN);
                    if (idx >= 0) {
                        domains.add(cursor.getString(idx));
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return domains;
    }

    public void removeBlacklistDomain(String domain) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = DatabaseContract.BlacklistEntry.COL_DOMAIN + " = ?";
        String[] selectionArgs = { domain.toLowerCase() };
        try {
            db.delete(DatabaseContract.BlacklistEntry.TABLE_NAME, selection, selectionArgs);
        } finally {
            db.close();
        }
    }
}
