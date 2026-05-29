package com.example.safelink.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import com.example.safelink.models.HistoryModel;

import java.util.ArrayList;
import java.util.List;

public class HistoryRepository {

    private final DatabaseHelper dbHelper;

    public HistoryRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public synchronized long insert(HistoryModel history) {
        SQLiteDatabase db = dbHelper.getWritableDatabase(DatabaseHelper.DB_PASSWORD);
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.HistoryEntry.COL_URL, history.getUrl());
        values.put(DatabaseContract.HistoryEntry.COL_STATUS, history.getStatus());
        values.put(DatabaseContract.HistoryEntry.COL_RISK_LEVEL, history.getRiskLevel());
        values.put(DatabaseContract.HistoryEntry.COL_RECOMMEND, history.getRecommendation());
        values.put(DatabaseContract.HistoryEntry.COL_SCANNED_AT, history.getScannedAt());
        
        long id = db.insert(DatabaseContract.HistoryEntry.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public synchronized List<HistoryModel> getAll() {
        List<HistoryModel> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase(DatabaseHelper.DB_PASSWORD);
        Cursor cursor = db.query(
                DatabaseContract.HistoryEntry.TABLE_NAME,
                null, null, null, null, null,
                DatabaseContract.HistoryEntry.COL_SCANNED_AT + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                list.add(fromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public synchronized List<HistoryModel> getRecent(int limit) {
        List<HistoryModel> list = new ArrayList<>();
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase(DatabaseHelper.DB_PASSWORD);
            Cursor cursor = db.query(
                    DatabaseContract.HistoryEntry.TABLE_NAME,
                    null, null, null, null, null,
                    DatabaseContract.HistoryEntry.COL_SCANNED_AT + " DESC",
                    String.valueOf(limit)
            );

            if (cursor.moveToFirst()) {
                do {
                    list.add(fromCursor(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public synchronized int countAll() {
        SQLiteDatabase db = dbHelper.getReadableDatabase(DatabaseHelper.DB_PASSWORD);
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseContract.HistoryEntry.TABLE_NAME, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public synchronized int countByStatus(String status) {
        SQLiteDatabase db = dbHelper.getReadableDatabase(DatabaseHelper.DB_PASSWORD);
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseContract.HistoryEntry.TABLE_NAME + 
                " WHERE " + DatabaseContract.HistoryEntry.COL_STATUS + " = ?",
                new String[]{status}
        );
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public synchronized void delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase(DatabaseHelper.DB_PASSWORD);
        db.delete(DatabaseContract.HistoryEntry.TABLE_NAME,
                DatabaseContract.HistoryEntry.COL_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public synchronized void clearAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase(DatabaseHelper.DB_PASSWORD);
        db.delete(DatabaseContract.HistoryEntry.TABLE_NAME, null, null);
        db.close();
    }

    public synchronized HistoryModel findByUrl(String url) {
        SQLiteDatabase db = dbHelper.getReadableDatabase(DatabaseHelper.DB_PASSWORD);
        Cursor cursor = db.query(
                DatabaseContract.HistoryEntry.TABLE_NAME,
                null,
                DatabaseContract.HistoryEntry.COL_URL + " = ?",
                new String[]{url},
                null, null,
                DatabaseContract.HistoryEntry.COL_SCANNED_AT + " DESC",
                "1"
        );

        HistoryModel item = null;
        if (cursor.moveToFirst()) {
            item = fromCursor(cursor);
        }
        cursor.close();
        db.close();
        return item;
    }

    private HistoryModel fromCursor(Cursor cursor) {
        return new HistoryModel(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.HistoryEntry.COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.HistoryEntry.COL_URL)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.HistoryEntry.COL_STATUS)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.HistoryEntry.COL_RISK_LEVEL)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.HistoryEntry.COL_RECOMMEND)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.HistoryEntry.COL_SCANNED_AT))
        );
    }
}
