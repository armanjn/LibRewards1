/*Author: Arman Jalilian
Date of Completion: 07/06/2020
Module Code: CSC3122
Application Name: Lib Rewards
Application Purpose: Rewards students as they spend time at the library
Class Name: ListFromFile
Class Purpose: This class reads all the files that are within the 'assets' directory. The methods within return the contents of the text file
to a list.
 */
package com.example.librewards

import android.content.Context
import java.util.*

class ListFromFile     //Parameter of 'Context' to state to the list which activity will be using the class
(private val context: Context) {
    //Method to read each line of a text file that is being read and assign the lines in the file to a list
    fun readLine(path: String?): List<String> {
        var lines: MutableList<String> = ArrayList()
        val inputStream: String = context.assets.open(path!!).bufferedReader().use { it.readText() }
        lines = inputStream.split("\r\n").toMutableList()
        //Returns the list of values
        return lines
    }


    //Method to read each line of a text file, split the contents of the line in to two and assign the contents to a list.
    //This method will only be used to get the rewards codes as the costs are assigned to the codes and separated with a comma
    fun readRewardsLine(path: String?): List<String> {
        var lines: MutableList<String> = ArrayList()
        val inputStream: String = context.assets.open(path!!).bufferedReader().use { it.readText() }
        lines = inputStream.split(",","\r\n").toMutableList()
        return lines
    }
}