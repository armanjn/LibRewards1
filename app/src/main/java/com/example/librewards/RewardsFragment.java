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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class RewardsFragment extends Fragment {
    private static final String TAG = RewardsFragment.class.getSimpleName();
    Dialog popup;
    DatabaseHelper myDb;

    private ListFromFile listFromFile;
    private EditText editText;
    private TextView points;
    private TextView name;
    private Button rewardButton;
    public List<String> currRewardCodes = new ArrayList<>();
    public List<String> rewardsCodes = new ArrayList<>();

    RewardsFragment.RewardsListener listener;
    private String textToEdit;

    public interface RewardsListener {
        void onPointsRewardsSent(int points);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_rewards, container, false);

        myDb = new DatabaseHelper(getActivity().getApplicationContext());
        rewardButton = v.findViewById(R.id.rewardButton);
        editText = v.findViewById(R.id.rewardText);
        points = v.findViewById(R.id.points2);
        points.setText(String.valueOf(myDb.getPoints()));
        name = v.findViewById(R.id.nameRewards);
        name.setText("Hey, " + myDb.getName());

        SharedPreferences rewardsPrefs = getActivity().getSharedPreferences("rewardsPrefs", Context.MODE_PRIVATE);
        boolean firstStart = rewardsPrefs.getBoolean("firstStart", true);
        if (firstStart) {
            addInitialCodes();
        }

        rewardsCodes = addNewCodes("rewardcodes.txt");
        myDb.updateRewardCodes(rewardsCodes);

        rewardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText.length() == 0){
                    toastMessage("No Code Was Entered");
                }
                if(rewardsCodes.contains(editText.getText().toString())){
                    myDb.minusPoints(myDb.getCost(editText.getText().toString()));
                    showPopup("Code accepted, keep it up! Your new points balance is: " + myDb.getPoints());
                    points.setText(String.valueOf(myDb.getPoints()));
                    listener.onPointsRewardsSent(myDb.getPoints());
                }
            }
        });
        return v;
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

    public void initialSetName(){
        name.setText("Hey, "+ myDb.getName());
    }

    private List<String> addNewCodes(String path){
        List<String> newList;
        listFromFile = new ListFromFile(getActivity().getApplicationContext());
        newList = listFromFile.readRewardsLine(path);
        for (String s : newList)
            Log.d(TAG, s);
        return newList;
    }


    private void addInitialCodes(){
        List<String> startList;
        listFromFile = new ListFromFile(getActivity().getApplicationContext());
        startList = listFromFile.readRewardsLine("rewardcodes.txt");
        for (String s : startList)
            Log.d(TAG, s);

        myDb.storeRewards(startList);

        SharedPreferences rewardsPrefs = getActivity().getSharedPreferences("rewardsPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = rewardsPrefs.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
    }

    public void toastMessage(String message){
        Toast.makeText(getActivity().getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }

    public void setTextToEdit(String textToEdit) {
        this.textToEdit = textToEdit;
    }

    public String getTextToEdit() {
        return textToEdit;
    }

    public void updatedPoints(int newPoints){
        points.setText(String.valueOf(newPoints));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof RewardsFragment.RewardsListener) {
            listener = (RewardsFragment.RewardsListener) context;
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
