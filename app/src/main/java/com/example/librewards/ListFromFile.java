/*Author: Arman Jalilian
Date of Completion: 07/06/2020
Module Code: CSC3122
Application Name: Lib Rewards
Application Purpose: Rewards students as they spend time at the library
Class Name: ListFromFile
Class Purpose: This class reads all the files that are within the 'assets' directory. The methods within return the contents of the text file
to a list.
 */
package com.example.librewards;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ListFromFile {

    private Context context;
    //Parameter of 'Context' to state to the list which activity will be using the class
    public ListFromFile(Context context) {
        this.context = context;
    }
    //Method to read each line of a text file that is being read and assign the lines in the file to a list
    public List<String> readLine(String path) {
        List<String> lines = new ArrayList<>();
        //Makes the assets in the activity assigned to the method available for use
        AssetManager am = context.getAssets();

        try {
            //Makes the text file available to be read from
            InputStream inputStream = am.open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            //Adds each line in the text file to the 'lines' list
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            inputStream.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Returns the list of values
        return lines;
    }
    //Method to read each line of a text file, split the contents of the line in to two and assign the contents to a list.
    //This method will only be used to get the rewards codes as the costs are assigned to the codes and separated with a comma
    public List<String> readRewardsLine(String path) {
        List<String> lines = new ArrayList<>();
        //Makes the assets in the activity assigned to the method available for use
        AssetManager am = context.getAssets();

        try {
            //Makes the text file available to be read from
            InputStream inputStream = am.open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                //Splits the line into two using a comma and adds to an array
                String[] split = line.split(",");
                //Iterates through the array and adds to the list
                for(String s : split) {
                    lines.add(s);

                }
            }
            inputStream.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Returns the list of values
        return lines;
    }
}