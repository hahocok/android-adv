package com.android.android.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class WeatherTable {
    private static final String TABLE_NAME = "weather";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_CITY = "city";
    private static final String COLUMN_DATA = "data";
    private static final String COLUMN_DATE = "date";

    public static void createTable(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CITY + " INTEGER, "
                + COLUMN_DATA + " TEXT);");
    }

    public static void onUpgrade(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        createTable(database);
    }

    public static void addData(String city, String data, SQLiteDatabase database) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_CITY, city);
        values.put(COLUMN_DATA, data);

        database.insert(TABLE_NAME, null, values);
    }

    public static void editData(String cityToEdit, String data, SQLiteDatabase database) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_DATA, data);
        database.update(TABLE_NAME, values, COLUMN_CITY + "=" + cityToEdit, null);
    }

    public static void deleteData(String  city, SQLiteDatabase database) {
        database.delete(TABLE_NAME, COLUMN_CITY + " = " + city, null);
    }

    public static void deleteAll(SQLiteDatabase database) {
        database.delete(TABLE_NAME, null, null);
    }

    public static List<String> getDataFromCity(String city, SQLiteDatabase database) {
        final Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + city, null);
        return getResultFromCursor(cursor);
    }

    private static List<String> getResultFromCursor(Cursor cursor) {
        List<String > result = null;

        if(cursor != null && cursor.moveToFirst()) {
            result = new ArrayList<>(cursor.getCount());

            int noteIdx = cursor.getColumnIndex(COLUMN_CITY);
            do {
                result.add(cursor.getString(noteIdx));
            } while (cursor.moveToNext());
        }

        try { cursor.close(); } catch (Exception ignored) {}
        return result == null ? new ArrayList<String>(0) : result;
    }
}
