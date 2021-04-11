/*Author: Arman Jalilian
Date of Completion: 07/06/2020
Module Code: CSC3122
Application Name: Lib Rewards
Application Purpose: Rewards students as they spend time at the library
Class Name: DatabaseHelper
Class Purpose: The database helper handles all of the back-end elements of the application using SQLite. SQLite is a language the aids with
local databases and any value that wants to be stored in a database would need to use methods inside this class.
 */
package com.example.librewards

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    //Method that creates the tables and the columns within where the columns have been given data types and names.
    override fun onCreate(db: SQLiteDatabase) {
        val table1 = "CREATE TABLE $TABLE1 (id INTEGER PRIMARY KEY AUTOINCREMENT,codes TEXT) "
        val table2 = "CREATE TABLE $TABLE2 (id INTEGER PRIMARY KEY AUTOINCREMENT,codes TEXT) "
        val table3 = "CREATE TABLE $TABLE3 (id INTEGER PRIMARY KEY AUTOINCREMENT,codes TEXT, cost INTEGER) "
        val table4 = "CREATE TABLE $TABLE4 (id INTEGER PRIMARY KEY AUTOINCREMENT,points INTEGER)"
        val table5 = "CREATE TABLE $TABLE5 (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT)"
        db.execSQL(table1)
        db.execSQL(table2)
        db.execSQL(table3)
        db.execSQL(table4)
        db.execSQL(table5)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE1")
        db.execSQL("DROP TABLE IF EXISTS $TABLE2")
        db.execSQL("DROP TABLE IF EXISTS $TABLE3")
        db.execSQL("DROP TABLE IF EXISTS $TABLE4")
        onCreate(db)
    }

    //Method that adds the name that the user gives to the database.
    fun addName(yourName: String?) {
        val db = this.writableDatabase
        val sql = "INSERT INTO $TABLE5(name)VALUES (?)"
        db.beginTransaction()
        val stmt = db.compileStatement(sql)
        stmt.bindString(1, yourName)
        stmt.execute()
        stmt.clearBindings()
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    //Cursor method that goes through the contents of a given column and table and returns values within them
    fun getAllData(col: String, table: String): Cursor {
        val db = this.writableDatabase
        return db.rawQuery("SELECT $col FROM $table", null)
    }

    //Method that returns the name that a user gives using a cursor
    val name: String
        get() {
            val db = this.writableDatabase
            var output = ""
            val c = db.rawQuery("SELECT name FROM $TABLE5", null)
            if (c != null && c.count > 0) {
                if (c.moveToFirst()) {
                    output = c.getString(c.getColumnIndex("name"))
                }
            }
            c!!.close()
            return output
        }

    //Method that returns points that a user has accumulated using a cursor
    val myPoints: Int
        get() {
            val db = this.writableDatabase
            var output = 0
            val c = db.rawQuery("SELECT points FROM $TABLE4", null)
            if (c != null && c.count > 0) {
                if (c.moveToFirst()) {
                    output = c.getInt(0)
                }
            }
            c!!.close()
            return output
        }

    //Method that returns the cost of a reward code that a user inputs
    fun getCost(code: String): Int {
        val db = this.writableDatabase
        var output = 0
        val c = db.rawQuery("SELECT cost FROM $TABLE3 WHERE codes = '$code'", null)
        if (c != null && c.count > 0) {
            if (c.moveToFirst()) {
                output = c.getInt(c.getColumnIndex("cost"))
            }
        }
        c!!.close()
        return output
    }

    //Method that updates the codes in the database by taking in a table name and a list of codes that has been read from a file
    fun updateCodes(table: String?, newCodesList: List<String?>) {
        var id = 1
        val db = this.writableDatabase
        val contentValues = ContentValues()
        for (i in newCodesList) {
            contentValues.put("codes", i)
            //Uses the 'id' column to iterate through the list of codes and update each one
            db.update(table, contentValues, "id = ?", arrayOf(id.toString()))
            //Iterates through codes by incrementing each id. Each id is assigned to a code and has always got a value
            id++
        }
    }

    //Method that updates the reward codes if the text file is different to the one stored in the database
    fun updateRewardCodes(newCodesList: List<String?>) {
        var id = 1
        //'j' is the integer that gets the cost of each code
        var j = 1
        val db = this.writableDatabase
        val contentValues = ContentValues()
        //'i' gets the code in the table. Each value increments by two as each two incremented values belong in the same column
        var i = 0
        while (i < newCodesList.size - 1) {
            contentValues.put("codes", newCodesList[i])
            contentValues.put("cost", newCodesList[j])
            //Uses the 'id' column to iterate through the list of codes and update each one
            db.update(TABLE3, contentValues, "id = ?", arrayOf(id.toString()))
            //Iterates through codes by incrementing each id. Each id is assigned to a code and has always got a value
            id++
            j += 2
            i += 2
        }
    }

    //Method that adds points to the current balance of points
    fun addPoints(points: Int) {
        val id = 1
        val db = this.writableDatabase
        val contentValues = ContentValues()
        //Uses the current balance and updates the balance with the sum of he points being passed in
        contentValues.put("points", myPoints + points)
        db.update(TABLE4, contentValues, "id = ?", arrayOf(id.toString()))
    }

    //Method that minuses points to the current balance of points
    fun minusPoints(points: Int) {
        val id = 1
        val db = this.writableDatabase
        var contentValues = ContentValues()
        contentValues.put("points", myPoints - points)
        db.update(TABLE4, contentValues, "id = ?", arrayOf(id.toString()))
    }

    //Method that stores the reward codes and their cost
    fun storeRewards(rewardList: List<String?>) {
        val db = this.writableDatabase
        //Stores the contents in the two columns specified for the column.
        val sql = "INSERT INTO $TABLE3(codes,cost)VALUES (?,?)"
        db.beginTransaction()
        //j is the integer that gets the cost of each code
        var j = 1
        val stmt = db.compileStatement(sql)
        //'i' gets the code in the table. Each value increments by two as each two incremented values belong in the same column
        var i = 0
        while (i < rewardList.size - 1) {

            //Values are assigned to each row in the table
            stmt.bindString(1, rewardList[i])
            stmt.bindString(2, rewardList[j])
            stmt.execute()
            stmt.clearBindings()
            j += 2
            i += 2
        }
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    //Method that deletes a given start/stop code from a given table that has been used so the user cannot use again
    fun deleteCode(table: String, code: String) {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $table WHERE codes=\"$code\";")
    }

    //Method that stores a list of start/stop codes in a given table
    fun storeCodes(codesList: List<String?>, table: String) {
        val db = this.writableDatabase
        val sql = "INSERT INTO $table(codes)VALUES (?)"
        db.beginTransaction()
        val stmt = db.compileStatement(sql)
        for (i in codesList) {
            stmt.bindString(1, i)
            stmt.execute()
            stmt.clearBindings()
        }
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    //Method that only runs once in the TimerFragment to instantiate the points to zero on first start-up
    fun initialPoints() {
        val db = this.writableDatabase
        db.execSQL("INSERT INTO $TABLE4(points)VALUES (?)")
    }

    companion object {
        //Instantiating the database name and table names. Final values so they cannot be changed once they are created
        const val DATABASE_NAME = "codes.db"
        const val TABLE1 = "start_codes_table"
        const val TABLE2 = "stop_codes_table"
        const val TABLE3 = "reward_codes_table"
        const val TABLE4 = "points_table"
        const val TABLE5 = "name_table"
    }

    init {
        val db = this.writableDatabase
    }
}