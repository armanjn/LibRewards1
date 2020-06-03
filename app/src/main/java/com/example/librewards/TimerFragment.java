package com.example.librewards;

import android.app.Dialog;
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
    private ListFromFile listFromFile;
    private List<String> startCodesList;

    private EditText startText;
    private String textToEdit;
    private Button startButton;
    private Button stopButton;
    private List<String> stopCodesList = new ArrayList<>();


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

        listFromFile = new ListFromFile(getActivity().getApplicationContext());
        startCodesList = listFromFile.readLine(startCodePath);
        for (String s : startCodesList)
            Log.d(TAG, s);

        myDb.storeStartCodes(startCodesList);

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
