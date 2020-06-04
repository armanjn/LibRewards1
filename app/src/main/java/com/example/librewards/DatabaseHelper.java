package com.example.librewards;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "codes91.db";
    public static final String TABLE1 = "start_codes_table";
    public static final String TABLE2 = "stop_codes_table";
    public static final String TABLE3 = "reward_codes_table";
    public static final String TABLE4 = "points_table";


    public DatabaseHelper(Context context)  {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String table1 = "CREATE TABLE " + TABLE1 + " (id INTEGER PRIMARY KEY AUTOINCREMENT,codes INTEGER) ";
        String table2 = "CREATE TABLE " + TABLE2 + " (id INTEGER PRIMARY KEY AUTOINCREMENT,codes INTEGER) ";
        String table3 = "CREATE TABLE " + TABLE3 + " (id INTEGER PRIMARY KEY AUTOINCREMENT,codes INTEGER) ";
        String table4 = "CREATE TABLE " + TABLE4 + " (id INTEGER PRIMARY KEY AUTOINCREMENT,points INTEGER) ";
        db.execSQL(table1);
        db.execSQL(table2);
        db.execSQL(table3);
        db.execSQL(table4);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE1);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE2);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE3);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE4);
        onCreate(db);
    }

    public Cursor getAllData(String col, String table){
        SQLiteDatabase db = getWritableDatabase();
        Cursor res = db.rawQuery("SELECT " +  col  + " FROM " +  table, null);
        return res;
    }

    public void deleteData(String table){
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "DELETE FROM " + table;
        db.beginTransaction();

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void storeCodes(List<String> startCodesList, String table){
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT INTO " + table + '('+ "codes" + ')' +  "VALUES (?)";
        db.beginTransaction();

        SQLiteStatement stmt = db.compileStatement(sql);
        for(int i=0 ; i< startCodesList.size(); i++){
            stmt.bindString(1,startCodesList.get(i));
            stmt.execute();
            stmt.clearBindings();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }



    public boolean insertStartCodes(String startCodes){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("start_codes", startCodes);
        long result =  db.insert(TABLE1, null, contentValues);
        if (result == -1){
            return false;
        }
        else {
            return true;
        }
    }

    public boolean insertStopCodes(String stopCodes){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("stop_codes", stopCodes);
        long result =  db.insert(TABLE2, null, contentValues);
        if (result == -1){
            return false;
        }
        else {
            return true;
        }
    }

    public boolean insertRewardCodes(String rewardCodes){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("reward_codes", rewardCodes);
        long result =  db.insert(TABLE3, null, contentValues);
        if (result == -1){
            return false;
        }
        else {
            return true;
        }

    }


    public boolean insertPoints(int points){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("points", points);
        long result =  db.insert(TABLE4, null, contentValues);
        if (result == -1){
            return false;
        }
        else {
            return true;
        }

    }


}

