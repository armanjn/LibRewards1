package com.example.librewards;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class TimerFragment extends Fragment {
    Dialog popup;
    Chronometer stopwatch;
    DatabaseHelper myDb;

    public static final String TAG = TimerFragment.class.getSimpleName();

    private ListFromFile listFromFile;
    public List<String> currStartCodes = new ArrayList<String>();
    public List<String> originalStartCodes = new ArrayList<String>();
    public List<String> currStopCodes = new ArrayList<String>();
    public List<String> originalStopCodes = new ArrayList<String>();

    private EditText editText;
    private String textToEdit;
    private Button startButton;
    private Button stopButton;
    private TextView points;
    TimerListener listener;

    public interface TimerListener {
        void onPointsTimerSent(int points);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_timer, container, false);
        stopwatch = v.findViewById(R.id.stopwatch);
        editText = v.findViewById(R.id.startText);
        startButton = v.findViewById(R.id.startButton);
        stopButton = v.findViewById(R.id.stopButton);
        myDb = new DatabaseHelper(getActivity().getApplicationContext());
        points = v.findViewById(R.id.points);
        points.setText(String.valueOf(myDb.getPoints()));


        SharedPreferences timerPrefs = getActivity().getSharedPreferences("timerPrefs", Context.MODE_PRIVATE);
        boolean firstStart = timerPrefs.getBoolean("firstStart", true);
        if (firstStart) {
            addInitialCodes();
        }

        addCurrCodes(currStartCodes,"start_codes_table");
        addCurrCodes(currStopCodes,"stop_codes_table");
        originalStartCodes = addNewCodes("startcodes.txt");
        originalStopCodes = addNewCodes("stopcodes.txt");

        checkForUpdates(currStartCodes, originalStartCodes, "start_codes_table");
        checkForUpdates(currStopCodes,originalStopCodes, "stop_codes_table");

            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if(editText.length() == 0){
                    toastMessage("No Code Was Entered");
                    }
                    if (currStartCodes.contains(editText.getText().toString())) {
                        currStartCodes.remove(editText.getText().toString());
                        myDb.deleteCode("start_codes_table", editText.getText().toString());
                        stopwatch.setBase(SystemClock.elapsedRealtime());
                        stopwatch.start();
                        startButton.setVisibility(v.INVISIBLE);
                        stopButton.setVisibility(v.VISIBLE);
                        stopwatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                            @Override
                            public void onChronometerTick(Chronometer chronometer) {
                                if ((SystemClock.elapsedRealtime() - stopwatch.getBase()) >= 50000) {
                                    stopwatch.setBase(SystemClock.elapsedRealtime());
                                    stopwatch.stop();
                                    stopButton.setVisibility(v.INVISIBLE);
                                    startButton.setVisibility(v.VISIBLE);
                                    showPopup("No stop code was entered for 24 hours. The timer has been reset");
                                }
                                stopButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(editText.length() == 0){
                                            toastMessage("No Code Was Entered");
                                        }
                                        if (currStopCodes.contains(editText.getText().toString())) {
                                            currStopCodes.remove(editText.getText().toString());
                                            myDb.deleteCode("stop_codes_table", editText.getText().toString());
                                            long totalTime = SystemClock.elapsedRealtime() - stopwatch.getBase();
                                            setPointsFromTime(totalTime);
                                            stopwatch.setBase(SystemClock.elapsedRealtime());
                                            stopwatch.stop();
                                            listener.onPointsTimerSent(myDb.getPoints());
                                            stopButton.setVisibility(v.INVISIBLE);
                                            startButton.setVisibility(v.VISIBLE);

                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            });


        return v;

    }

    public void updatePoints(int newPoints){
        points.setText(String.valueOf(newPoints));
    }

    public void checkForUpdates(List<String> currCodes, List<String> originalCodes, String table){
        List<String> tempCodes = new ArrayList<>();
        for(int i = 0; i<currCodes.size(); i++){
            for (int j = 0; j<originalCodes.size(); j++){
                if(originalCodes.get(j).equals(currCodes.get(i))){
                    tempCodes.add(currCodes.get(i));
                }
            }
        }
        if(!(currCodes.equals(tempCodes))){
            myDb.updateCodes(table,originalCodes);
        }

    }
    private void addCurrCodes(List<String> codeList, String table) {
        Cursor c = myDb.getAllData("codes", table);
        c.moveToFirst();
        while(!c.isAfterLast()) {
            codeList.add((String) c.getString(c.getColumnIndex("codes")));
            c.moveToNext();
        }
    }

    public void setPointsFromTime(long totalTime){
        int pointsEarned = 0;
        if(totalTime > 10000 && totalTime < 20000){
            pointsEarned = 30;
            myDb.addPoints(pointsEarned);
            points.setText(String.valueOf(myDb.getPoints()));

        }
        showPopup("You have earned: " + pointsEarned + " points, well done!");
    }

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
    private List<String> addNewCodes(String path){
        List<String> newList;
        listFromFile = new ListFromFile(getActivity().getApplicationContext());
        newList = listFromFile.readLine(path);
        for (String s : newList)
            Log.d(TAG, s);
        return newList;
    }

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
