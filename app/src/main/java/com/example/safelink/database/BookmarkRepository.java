package com.example.safelink.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import com.example.safelink.models.BookmarkModel;

import java.util.ArrayList;
import java.util.List;

public class BookmarkRepository {

    private final DatabaseHelper dbHelper;

    public BookmarkRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public synchronized long insert(BookmarkModel bookmark) {
        if (isBookmarked(bookmark.getUrl())) {
            return -1;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase(DatabaseHelper.DB_PASSWORD);
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.BookmarkEntry.COL_URL, bookmark.getUrl());
        values.put(DatabaseContract.BookmarkEntry.COL_TITLE, bookmark.getTitle());
        values.put(DatabaseContract.BookmarkEntry.COL_CATEGORY, bookmark.getCategory());
        values.put(DatabaseContract.BookmarkEntry.COL_NOTES, bookmark.getNotes());
        values.put(DatabaseContract.BookmarkEntry.COL_FAVICON, bookmark.getFaviconUrl());
        values.put(DatabaseContract.BookmarkEntry.COL_STATUS, bookmark.getStatus());
        values.put(DatabaseContract.BookmarkEntry.COL_SCANNED_AT, bookmark.getScannedAt());
        
        long id = db.insert(DatabaseContract.BookmarkEntry.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public synchronized List<BookmarkModel> getAll() {
        List<BookmarkModel> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase(DatabaseHelper.DB_PASSWORD);
        Cursor cursor = db.query(
                DatabaseContract.BookmarkEntry.TABLE_NAME,
                null, null, null, null, null,
                DatabaseContract.BookmarkEntry.COL_ID + " DESC"
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

    public synchronized void delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase(DatabaseHelper.DB_PASSWORD);
        db.delete(DatabaseContract.BookmarkEntry.TABLE_NAME,
                DatabaseContract.BookmarkEntry.COL_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }
    
    public synchronized boolean isBookmarked(String url) {
        SQLiteDatabase db = dbHelper.getReadableDatabase(DatabaseHelper.DB_PASSWORD);
        Cursor cursor = db.query(
                DatabaseContract.BookmarkEntry.TABLE_NAME,
                new String[]{DatabaseContract.BookmarkEntry.COL_ID},
                DatabaseContract.BookmarkEntry.COL_URL + " = ?",
                new String[]{url},
                null, null, null
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    private BookmarkModel fromCursor(Cursor cursor) {
        return new BookmarkModel(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.BookmarkEntry.COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.BookmarkEntry.COL_URL)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.BookmarkEntry.COL_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.BookmarkEntry.COL_CATEGORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.BookmarkEntry.COL_NOTES)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.BookmarkEntry.COL_FAVICON)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.BookmarkEntry.COL_STATUS)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.BookmarkEntry.COL_SCANNED_AT))
        );
    }
}
