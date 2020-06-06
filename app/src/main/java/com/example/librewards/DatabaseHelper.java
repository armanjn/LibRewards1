package com.example.librewards;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "codes.db";
    public static final String TABLE1 = "start_codes_table";
    public static final String TABLE2 = "stop_codes_table";
    public static final String TABLE3 = "reward_codes_table";
    public static final String TABLE4 = "points_table";
    public static final String TABLE5 = "name_table";




    public DatabaseHelper(Context context)  {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String table1 = "CREATE TABLE " + TABLE1 + " (id INTEGER PRIMARY KEY AUTOINCREMENT,codes TEXT) ";
        String table2 = "CREATE TABLE " + TABLE2 + " (id INTEGER PRIMARY KEY AUTOINCREMENT,codes TEXT) ";
        String table3 = "CREATE TABLE " + TABLE3 + " (id INTEGER PRIMARY KEY AUTOINCREMENT,codes TEXT, cost INTEGER) ";
        String table4 = "CREATE TABLE " + TABLE4 + " (id INTEGER PRIMARY KEY AUTOINCREMENT,points INTEGER)";
        String table5 = "CREATE TABLE " + TABLE5 + " (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT)";
        db.execSQL(table1);
        db.execSQL(table2);
        db.execSQL(table3);
        db.execSQL(table4);
        db.execSQL(table5);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE1);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE2);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE3);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE4);
        onCreate(db);
    }

    public void addName(String yourName){
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = ("INSERT INTO " + TABLE5 + '(' + "name" + ')' + "VALUES (?)");
        db.beginTransaction();
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindString(1,yourName);
        stmt.execute();
        stmt.clearBindings();
        db.setTransactionSuccessful();
        db.endTransaction();

    }

    public Cursor getAllData(String col, String table){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " +  col  + " FROM " +  table, null);
        return c;
    }

    public String getName(){
        SQLiteDatabase db = this.getWritableDatabase();
        String output = "";
        Cursor c = db.rawQuery("SELECT " + "name" + " FROM " + TABLE5, null);
        if(c != null && c.getCount() > 0) {
            if(c.moveToFirst()){
                output = c.getString(c.getColumnIndex("name"));
            }
        }
        c.close();
        return output;
    }

    public int getPoints(){
        SQLiteDatabase db = this.getWritableDatabase();
        int output = 0;
        Cursor c = db.rawQuery("SELECT " + "points" + " FROM " + TABLE4, null);
        if(c != null && c.getCount() > 0){
            if(c.moveToFirst()){

                output = c.getInt(0);

            }
        }

        c.close();
        return output;
    }

    public int getCost(String code) {
        SQLiteDatabase db = this.getWritableDatabase();
        int output = 0;
        Cursor c = db.rawQuery("SELECT " + "cost" + " FROM " + TABLE3 + " WHERE codes = " + "'" + code + "'", null);
        if (c != null && c.getCount() > 0) {
            if (c.moveToFirst()) {

                output = c.getInt(c.getColumnIndex("cost"));

            }
        }
        c.close();
        return output;
    }

    public void updateCodes(String table, List<String> newCodesList) {
        int id = 1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < newCodesList.size(); i++) {
            contentValues.put("codes", newCodesList.get(i));
            db.update(table, contentValues, "id = ?", new String[]{String.valueOf(id)});
            id++;
        }
    }

    public void updateRewardCodes(List<String> newCodesList) {
        int id = 1;
        int j = 1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < newCodesList.size()-1; i+=2) {
            contentValues.put("codes", newCodesList.get(i));
            contentValues.put("cost", newCodesList.get(j));
            db.update(TABLE3, contentValues, "id = ?", new String[]{String.valueOf(id)});
            id++;
            j+=2;
        }
    }

    public void addPoints(int points){
        int id = 1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("points", getPoints()+ points);
        db.update(TABLE4, contentValues, "id = ?", new String[]{String.valueOf(id)});
    }

    public void storeRewards(List<String> rewardList){
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT INTO " + TABLE3 + '('+ "codes,cost" + ')' +  "VALUES (?,?)";
        db.beginTransaction();
        int j = 1;
        SQLiteStatement stmt = db.compileStatement(sql);
        for(int i=0 ; i< rewardList.size()-1; i+=2) {
                stmt.bindString(1, rewardList.get(i));
                stmt.bindString(2, rewardList.get(j));
                stmt.execute();
                stmt.clearBindings();
                j+=2;
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void minusPoints(int points){
        int id = 1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("points", getPoints() - points);
        db.update(TABLE4, contentValues, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteCode(String table, String code){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + table + " WHERE "+ "codes" + "=\"" + code + "\";");
    }

    public void storeCodes(List<String> codesList, String table){
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT INTO " + table + '('+ "codes" + ')' +  "VALUES (?)";
        db.beginTransaction();

        SQLiteStatement stmt = db.compileStatement(sql);
        for(int i=0 ; i< codesList.size(); i++){
            stmt.bindString(1,codesList.get(i));
            stmt.execute();
            stmt.clearBindings();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }


    public void initialPoints(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO " + TABLE4 + '(' + "points" + ')' + "VALUES ('0')");
    }


}

