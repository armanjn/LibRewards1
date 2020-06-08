/*Author: Arman Jalilian
Date of Completion: 07/06/2020
Module Code: CSC3122
Application Name: Lib Rewards
Application Purpose: Rewards students as they spend time at the library
Class Name: DatabaseHelper
Class Purpose: The database helper handles all of the back-end elements of the application using SQLite. SQLite is a language the aids with
local databases and any value that wants to be stored in a database would need to use methods inside this class.
 */
package com.example.librewards;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    //Instantiating the database name and table names. Final values so they cannot be changed once they are created
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
    //Method that creates the tables and the columns within where the columns have been given data types and names.
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
    //Method that adds the name that the user gives to the database.
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
    //Cursor method that goes through the contents of a given column and table and returns values within them
    public Cursor getAllData(String col, String table){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " +  col  + " FROM " +  table, null);
        return c;
    }

    //Method that returns the name that a user gives using a cursor
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

    //Method that returns points that a user has accumulated using a cursor
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

    //Method that returns the cost of a reward code that a user inputs
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

    //Method that updates the codes in the database by taking in a table name and a list of codes that has been read from a file
    public void updateCodes(String table, List<String> newCodesList) {
        int id = 1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < newCodesList.size(); i++) {
            contentValues.put("codes", newCodesList.get(i));
            //Uses the 'id' column to iterate through the list of codes and update each one
            db.update(table, contentValues, "id = ?", new String[]{String.valueOf(id)});
            //Iterates through codes by incrementing each id. Each id is assigned to a code and has always got a value
            id++;
        }
    }
    //Method that updates the reward codes if the text file is different to the one stored in the database
    public void updateRewardCodes(List<String> newCodesList) {
        int id = 1;
        //'j' is the integer that gets the cost of each code
        int j = 1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //'i' gets the code in the table. Each value increments by two as each two incremented values belong in the same column
        for (int i = 0; i < newCodesList.size()-1; i+=2) {
            contentValues.put("codes", newCodesList.get(i));
            contentValues.put("cost", newCodesList.get(j));
            //Uses the 'id' column to iterate through the list of codes and update each one
            db.update(TABLE3, contentValues, "id = ?", new String[]{String.valueOf(id)});
            //Iterates through codes by incrementing each id. Each id is assigned to a code and has always got a value
            id++;
            j+=2;
        }
    }
    //Method that adds points to the current balance of points
    public void addPoints(int points){
        int id = 1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //Uses the current balance and updates the balance with the sum of he points being passed in
        contentValues.put("points", getPoints()+ points);
        db.update(TABLE4, contentValues, "id = ?", new String[]{String.valueOf(id)});
    }
    //Method that minuses points to the current balance of points
    public void minusPoints(int points){
        int id = 1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("points", getPoints() - points);
        db.update(TABLE4, contentValues, "id = ?", new String[]{String.valueOf(id)});
    }
    //Method that stores the reward codes and their cost
    public void storeRewards(List<String> rewardList){
        SQLiteDatabase db = this.getWritableDatabase();
        //Stores the contents in the two columns specified for the column.
        String sql = "INSERT INTO " + TABLE3 + '('+ "codes,cost" + ')' +  "VALUES (?,?)";
        db.beginTransaction();
        //j is the integer that gets the cost of each code
        int j = 1;
        SQLiteStatement stmt = db.compileStatement(sql);
        //'i' gets the code in the table. Each value increments by two as each two incremented values belong in the same column
        for(int i=0 ; i< rewardList.size()-1; i+=2) {
            //Values are assigned to each row in the table
                stmt.bindString(1, rewardList.get(i));
                stmt.bindString(2, rewardList.get(j));
                stmt.execute();
                stmt.clearBindings();
                j+=2;
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }
    //Method that deletes a given start/stop code from a given table that has been used so the user cannot use again
    public void deleteCode(String table, String code){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + table + " WHERE "+ "codes" + "=\"" + code + "\";");
    }

    //Method that stores a list of start/stop codes in a given table
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

    //Method that only runs once in the TimerFragment to instantiate the points to zero on first start-up
    public void initialPoints(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO " + TABLE4 + '(' + "points" + ')' + "VALUES (?)");

    }


}

