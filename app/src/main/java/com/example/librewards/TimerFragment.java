package com.example.librewards;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

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

import java.util.ArrayList;
import java.util.List;

public class TimerFragment extends Fragment {
    Dialog popup;
    Chronometer stopwatch;
    DatabaseHelper myDb;

    public static final String TAG = TimerFragment.class.getSimpleName();

    public static final String startCodePath = "startcodes.txt";
    public static final String stopCodePath = "stop.txt";

    private ListFromFile listFromFile;
    public List<String> initialStartCodesList = new ArrayList<String>();
    public List<String> currStartCodesList = new ArrayList<String>();
    public List<String> newStartCodesList = new ArrayList<String>();


    private EditText startText;
    private String textToEdit;
    private Button startButton;
    private Button stopButton;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_timer, container, false);
        stopwatch = v.findViewById(R.id.stopwatch);
        startText = v.findViewById(R.id.startText);
        startButton = v.findViewById(R.id.startButton);
        stopButton = v.findViewById(R.id.stopButton);
        myDb = new DatabaseHelper(getActivity().getApplicationContext());

        SharedPreferences pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        boolean firstStart = pref.getBoolean("firstStart", true);
        if(firstStart){
            initialStartCodesList = addInitialCodes( "startcodes.txt");
        }

       Cursor c = myDb.getAllData("codes", "start_codes_table");
        c.moveToFirst();
        while(!c.isAfterLast()) {
            currStartCodesList.add(c.getString(c.getColumnIndex("codes")));
            c.moveToNext();
        }
        newStartCodesList = addNewCodes( "startcodes.txt");

        if(!(initialStartCodesList.equals(newStartCodesList))){
            initialStartCodesList = newStartCodesList;
            //myDb.deleteData("start_codes_table");
            myDb.storeCodes(newStartCodesList, "start_codes_table");
        }





        startButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v) {
                String newEntry = startText.getText().toString();
                if(startText.length() != 0){
                    addData(newEntry);
                    startText.setText("");
                }
                else{
                    toastMessage("No Code Was Entered");
                }
                stopwatch.setBase(SystemClock.elapsedRealtime());
                stopwatch.start();
                startButton.setVisibility(v.INVISIBLE);
                stopButton.setVisibility(v.VISIBLE);
                stopwatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                    @Override
                    public void onChronometerTick(Chronometer chronometer) {
                        if ((SystemClock.elapsedRealtime() - stopwatch.getBase()) >= 5000){
                            stopwatch.setBase(SystemClock.elapsedRealtime());
                            stopwatch.stop();
                            stopButton.setVisibility(v.INVISIBLE);
                            startButton.setVisibility(v.VISIBLE);
                            setTextToEdit("No stop code was entered for 24 hours. The stopwatch will now reset.");
                            showPopup();
                        }
                        stopButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                stopwatch.setBase(SystemClock.elapsedRealtime());
                                stopwatch.stop();
                                stopButton.setVisibility(v.INVISIBLE);
                                startButton.setVisibility(v.VISIBLE);
                            }
                        });
                    }
                });

            }
        });

        return v;

    }



    public void showToast() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) getActivity().findViewById(R.id.toastLayout));
        TextView toastText = layout.findViewById(R.id.toastText);
        toastText.setText(getTextToEdit());
        Toast toast = Toast.makeText(getActivity().getApplicationContext(), getTextToEdit(), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(layout);
        toast.show();
    }

    public void showPopup(){
        popup = new Dialog(getActivity());
        popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setContentView(R.layout.popup_layout);
        ImageView closeBtn = popup.findViewById(R.id.closeBtn);
        TextView popupText = popup.findViewById(R.id.popupText);
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

    private List<String> addInitialCodes(String path){
        List<String> list;
        listFromFile = new ListFromFile(getActivity().getApplicationContext());
        list = listFromFile.readLine(path);
        for (String s : list)
            Log.d(TAG, s);

        myDb.storeCodes(list, "start_codes_table");

        SharedPreferences pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
        return list;
    }

    public void setTextToEdit(String textToEdit) {
        this.textToEdit = textToEdit;
    }

    public String getTextToEdit() {
        return textToEdit;
    }

    public void addData(String newEntry){
        boolean insertData = myDb.insertStartCodes( newEntry);

        if(insertData){
            toastMessage("Data Successfully Added");
        }
        else{
            toastMessage("Something went wrong");
        }
    }


    public void toastMessage(String message){
        Toast.makeText(getActivity().getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }
}
