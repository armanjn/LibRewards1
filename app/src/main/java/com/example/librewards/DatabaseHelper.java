package com.example.librewards;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "codes.db";
    public static final String TABLE_NAME = "start_codes_table";
    public static final String COL_1 = "START_CODES";
    public static final String TABLE_2_NAME = "stop_codes_table";
    public static final String COL_2 = "START_CODES";
    public static final String TABLE_3_NAME = "reward_codes_table";
    public static final String COL_3 = "REWARD_CODES";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (TIMER_START_CODES INTEGER PRIMARY KEY) ");
        db.execSQL("create table " + TABLE_2_NAME + " (TIMER_STOP_CODES INTEGER PRIMARY KEY) ");
        db.execSQL("create table " + TABLE_3_NAME + " (REWARD_CODES INTEGER PRIMARY KEY) ");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS '"+ TABLE_NAME + "'");
        db.execSQL("DROP TABLE IF EXISTS '"+ TABLE_2_NAME + "'");
        db.execSQL("DROP TABLE IF EXISTS '"+ TABLE_3_NAME + "'");
        onCreate(db);
    }
}
