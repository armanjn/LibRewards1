/*Author: Arman Jalilian
Date of Completion: 07/06/2020
Module Code: CSC3122
Application Name: Lib Rewards
Application Purpose: Rewards students as they spend time at the library
Class Name: TimerFragment
Class Purpose: The starting fragment for when a user opens the application. It allows the user to enter start and stop code given by
library staff for the day. The start code will start the timer and the stop code will stop. The duration spent at the library is then
converted into points where a user can later redeem in RewardsFragment.
 */

package com.example.librewards;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.TimeUnit;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.sql.Time;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TimerFragment extends Fragment {
    Dialog popup;
    Chronometer stopwatch;
    DatabaseHelper myDb;

    public static final String TAG = TimerFragment.class.getSimpleName();

    private ListFromFile listFromFile;
    public List<String> currStartCodes = new ArrayList<>();
    public List<String> originalStartCodes = new ArrayList<>();
    public List<String> currStopCodes = new ArrayList<>();
    public List<String> originalStopCodes = new ArrayList<>();

    private EditText editText;
    private String textToEdit;
    private Button startButton;
    private Button stopButton;
    private TextView points;
    private TextView name;
    TimerListener listener;

    //Interface that consists of a method that will update the points in "RewardsFragment"
    public interface TimerListener {
        void onPointsTimerSent(int points);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_timer, container, false);
        //Assigns the field to the view's specified in the fragment_timer XML file file
        stopwatch = v.findViewById(R.id.stopwatch);
        editText = v.findViewById(R.id.startText);
        startButton = v.findViewById(R.id.startButton);
        stopButton = v.findViewById(R.id.stopButton);
        myDb = new DatabaseHelper(getActivity().getApplicationContext());
        points = v.findViewById(R.id.points);
        points.setText(String.valueOf(myDb.getPoints()));
        name = v.findViewById(R.id.nameTimer);
        //Sets the name of user for this fragment by retrieving it from the database
        String wholeName = getString(R.string.Hey) + " " +myDb.getName();
        name.setText(wholeName);

        //Creating a preference for activity on first start-up only
        SharedPreferences timerPrefs = getActivity().getSharedPreferences("timerPrefs", Context.MODE_PRIVATE);
        boolean firstStart = timerPrefs.getBoolean("firstStart", true);
        //Anything enclosed in the 'if' statement will only run once; at first start-up.
        if (firstStart) {
            myDb.initialPoints();
            addInitialCodes();

        }

        //Gets all of the codes that are currently in the database and adds them to a list
        addCurrCodes(currStartCodes,"start_codes_table");
        addCurrCodes(currStopCodes,"stop_codes_table");
        //Gets all of the codes listed in the text files and add them to a list
        originalStartCodes = addNewCodes("startcodes.txt");
        originalStopCodes = addNewCodes("stopcodes.txt");
        //Checks if the text files have any codes different to the ones currently in the database and updates the
        //database if so. This is the method that would be used once the codes need to be refreshed. This
        //would happen every once in a while
        checkForUpdates(currStartCodes, originalStartCodes, "start_codes_table");
        checkForUpdates(currStopCodes,originalStopCodes, "stop_codes_table");
            //Sets actions on clicking the "Start" Button
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    //Checks if there is any text inputted
                    if(editText.length() == 0){
                    toastMessage("No code was entered, please try again");
                    }
                    //Checks if the current start code table in the database contains the code that has been inputted
                    else if (currStartCodes.contains(editText.getText().toString())) {
                        //Removes the code from the database as it has already been used once
                        currStartCodes.remove(editText.getText().toString());
                        myDb.deleteCode("start_codes_table", editText.getText().toString());
                        //Clears the input text
                        editText.setText(null);
                        editText.setHint("Please enter the stop code");
                        //Starts the stopwatch
                        stopwatch.setBase(SystemClock.elapsedRealtime());
                        stopwatch.start();
                        //Switches from the 'Start' button to the 'Stop' button
                        startButton.setVisibility(v.INVISIBLE);
                        stopButton.setVisibility(v.VISIBLE);
                        //All actions to be taken place once the stopwatch has started
                        stopwatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                            @Override
                            public void onChronometerTick(Chronometer chronometer) {
                                //Checks if the stopwatch has gone over 24 hours. If so, the stopwatch resets back to its original state
                                if ((SystemClock.elapsedRealtime() - stopwatch.getBase()) >= 800000) {
                                    stopwatch.setBase(SystemClock.elapsedRealtime());
                                    stopwatch.stop();
                                    stopButton.setVisibility(v.INVISIBLE);
                                    startButton.setVisibility(v.VISIBLE);
                                    showPopup("No stop code was entered for 24 hours. The timer has been reset");
                                }
                                stopButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //Checks if there is any text inputted
                                        if(editText.length() == 0){
                                            toastMessage("No code was entered");
                                        }
                                        //Checks if the current stop code table in the database contains the code that has been inputted
                                        if (currStopCodes.contains(editText.getText().toString())) {
                                            //Removes the code from the database as it has already been used once
                                            currStopCodes.remove(editText.getText().toString());
                                            myDb.deleteCode("stop_codes_table", editText.getText().toString());
                                            //Clears the input text and resets to original state
                                            editText.setText(null);
                                            editText.setHint("Please enter the start code");
                                            //'totalTime' gets the total duration spent at the library in milliseconds
                                            long totalTime = SystemClock.elapsedRealtime() - stopwatch.getBase();
                                            //Sets the points using the setPoints method
                                            setPointsFromTime(totalTime);
                                            stopwatch.setBase(SystemClock.elapsedRealtime());
                                            stopwatch.stop();
                                            //Listener to communicate with Rewards Fragment and give the points to display in there
                                            listener.onPointsTimerSent(myDb.getPoints());
                                            stopButton.setVisibility(v.INVISIBLE);
                                            startButton.setVisibility(v.VISIBLE);

                                        }
                                        //If the stop code entered is not in the database, a toast will show
                                        else{
                                            toastMessage("The code you entered is not valid, please try again");
                                        }
                                    }
                                });
                            }
                        });
                    }
                    //If the start code entered is not in the database, a toast will show
                    else{
                        toastMessage(getString(R.string.invalidCode));
                    }
                }
            });


        return v;

    }

    //Method that is used between fragments to update each other's points
    public void updatePoints(int newPoints){
        points.setText(String.valueOf(newPoints));
    }
    //Method to check if the text file has been updated with new codes or not
    public void checkForUpdates(List<String> currCodes, List<String> originalCodes, String table){
        List<String> tempCodes = new ArrayList<>();
        //Loop to check if the elements in the 'currCodes' list exactly matches those in the text file. The ones that
        //match get added into a temporary list
        for(int i = 0; i<currCodes.size(); i++){
            for (int j = 0; j<originalCodes.size(); j++){
                if(originalCodes.get(j).equals(currCodes.get(i))){
                    tempCodes.add(currCodes.get(i));
                }
            }
        }
        //Temporary list is compared with the current codes list. If they are not an
        //exact match, the codes update using the method in the DatabaseHelper class
        if(!(currCodes.equals(tempCodes))){
            myDb.updateCodes(table,originalCodes);
        }

    }
    //Method to add the current codes that are in the database to a list
    private void addCurrCodes(List<String> codeList, String table) {
        Cursor c = myDb.getAllData("codes", table);
        c.moveToFirst();
        while(!c.isAfterLast()) {
            codeList.add(c.getString(c.getColumnIndex("codes")));
            c.moveToNext();
        }
    }

    //Method that converts the duration spent at the library into points
    public void setPointsFromTime(long totalTime){
        int pointsEarned = 0;
        int minutes = (int) ((totalTime/1000) /60);
        if(totalTime > 10000 && totalTime < 30000){
            pointsEarned = 10;
            myDb.addPoints(pointsEarned);
            points.setText(String.valueOf(myDb.getPoints()));

        }
        else if(totalTime >= 30000 && totalTime < 60000){
            pointsEarned = 50;
            myDb.addPoints(pointsEarned);
            points.setText(String.valueOf(myDb.getPoints()));
        }
        else if(totalTime >= 60000 && totalTime < 120000){
            pointsEarned = 75;
            myDb.addPoints(pointsEarned);
            points.setText(String.valueOf(myDb.getPoints()));
        }
        else if(totalTime >= 120000 && totalTime < 180000){
            pointsEarned = 125;
            myDb.addPoints(pointsEarned);
            points.setText(String.valueOf(myDb.getPoints()));
        }
        else if(totalTime >= 180000 && totalTime < 260000){
            pointsEarned = 225;
            myDb.addPoints(pointsEarned);
            points.setText(String.valueOf(myDb.getPoints()));
        }
        else if(totalTime >= 260000 && totalTime < 400000){
            pointsEarned = 400;
            myDb.addPoints(pointsEarned);
            points.setText(String.valueOf(myDb.getPoints()));
        }
        else if(totalTime >= 500000){
            pointsEarned = 700;
            myDb.addPoints(pointsEarned);
            points.setText(String.valueOf(myDb.getPoints()));
        }
        if(minutes == 1){
            showPopup("Well done, you spent "+ minutes +" minute at the library and have earned " + pointsEarned + " points!\nYour new points balance is: " + myDb.getPoints());

        }
        else{
            showPopup("Well done, you spent "+ minutes +" minutes at the library and have earned " + pointsEarned + " points!\nYour new points balance is: " + myDb.getPoints());

        }
    }
    //Method to set the name on first start-up. Method is called in MainActivity
    public void initialSetName(){
        name.setText(getString(R.string.Hey)+" "+ myDb.getName());
    }

    //Method that creates a popup
    public void showPopup(String text){
        popup = new Dialog(getActivity());
        popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setContentView(R.layout.popup_layout);
        ImageView closeBtn = popup.findViewById(R.id.closeBtn);
        TextView popupText = popup.findViewById(R.id.popupText);
        setTextToEdit(text);
        popupText.setText(getTextToEdit());
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
        popup.show();

    }
    //Method that adds the codes from the text file into a list using the ListFromFile class
    private List<String> addNewCodes(String path){
        List<String> newList;
        listFromFile = new ListFromFile(getActivity().getApplicationContext());
        newList = listFromFile.readLine(path);
        for (String s : newList)
            Log.d(TAG, s);
        return newList;
    }

    //Method adds codes to the database on first start-up
    private void addInitialCodes(){
        List<String> startList;
        listFromFile = new ListFromFile(getActivity().getApplicationContext());
        startList = listFromFile.readLine("startcodes.txt");
        for (String s : startList)
            Log.d(TAG, s);

        myDb.storeCodes(startList, "start_codes_table");

        List<String> stopList;
        stopList = listFromFile.readLine("stopcodes.txt");
        for (String d : stopList)
            Log.d(TAG, d);

        myDb.storeCodes(stopList, "stop_codes_table");

        //'firstStart' boolean is set to false which means that the the method will not run after first
        //start
        SharedPreferences timerPrefs = getActivity().getSharedPreferences("timerPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = timerPrefs.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
    }

    public void setTextToEdit(String textToEdit) {
        this.textToEdit = textToEdit;
    }

    public String getTextToEdit() {
        return textToEdit;
    }

    //Custom Toast message
    public void toastMessage(String message){
        Toast.makeText(getActivity().getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof TimerListener) {
            listener = (TimerListener) context;
        }
        else{
            throw new RuntimeException(context.toString() + "must implement TimerListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
