/*Author: Arman Jalilian
Date of Completion: 07/06/2020
Module Code: CSC3122
Application Name: Lib Rewards
Application Purpose: Rewards students as they spend time at the library
Class Name: RewardsFragment
Class Purpose: The rewards fragment is where a user can spend the points that they have earned. They can acquire a code from the library shop which will decrease the amount of
points, if they have enough. This will prove to the library staff that they have spent the points and therefore they can in return reward them.
 */

package com.example.librewards;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
    public List<String> rewardsCodes = new ArrayList<>();

    RewardsFragment.RewardsListener listener;
    private String textToEdit;

    //Interface that consists of a method that will update the points in "TimerFragment"
    public interface RewardsListener {
        void onPointsRewardsSent(int points);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_rewards, container, false);
        //Assigns the field to the view's specified in the fragment_timer XML file file
        myDb = new DatabaseHelper(getActivity().getApplicationContext());
        rewardButton = v.findViewById(R.id.rewardButton);
        editText = v.findViewById(R.id.rewardText);
        points = v.findViewById(R.id.points2);
        points.setText(String.valueOf(myDb.getPoints()));
        name = v.findViewById(R.id.nameRewards);
        //Sets the name of user for this fragment by retrieving it from the database
        String wholeName = getString(R.string.Hey) + " " + myDb.getName();
        name.setText(wholeName);

        //Creating a preference for activity on first start-up only
        SharedPreferences rewardsPrefs = getActivity().getSharedPreferences("rewardsPrefs", Context.MODE_PRIVATE);
        boolean firstStart = rewardsPrefs.getBoolean("firstStart", true);
        //Anything enclosed in the 'if' statement will only run once; at first start-up.
        if (firstStart) {
            addInitialCodes();
        }
        //Adds the codes from the text file to the database and updates the database every time in case there are new codes or costs in the text file
        rewardsCodes = addNewCodes("rewardcodes.txt");
        myDb.updateRewardCodes(rewardsCodes);

        //Sets actions on clicking the "Reward" Button
        rewardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Checks if the input text is empty
                if(editText.length() == 0){
                    toastMessage("No code was entered, please try again");
                }
                //Checks if the list containing the reward codes has the one inputted by the user
                else if(rewardsCodes.contains(editText.getText().toString())){
                    //Checks if the cost that comes with the code is greater than the points a user has
                    if(!(myDb.getPoints() <= myDb.getCost(editText.getText().toString()))){
                        //If the user has enough points, the cost is deducted from the points and the user can get their reward
                        myDb.minusPoints(myDb.getCost(editText.getText().toString()));
                        showPopup("Code accepted, keep it up! Your new points balance is: " + myDb.getPoints());
                        points.setText(String.valueOf(myDb.getPoints()));
                        //Communicates the new point balance with other fragment
                        listener.onPointsRewardsSent(myDb.getPoints());
                    }
                    //If the points are less than the cost of the reward, a popup shows up stating that the user has insufficient funds
                    else{
                        showPopup(getString(R.string.insufficientFunds));

                    }
                }
                //If code is incorrect, a Toast will pop up stating invalid code
                else{
                    toastMessage(getString(R.string.invalidCode));
                }
            }
        });
        return v;
    }
    //Method that creates a custom popup
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
    //Method to set the name on first start-up. Method is called in MainActivity
    public void initialSetName(){
        name.setText(getString(R.string.Hey)+ " "+  myDb.getName());
    }
    //Method that adds new reward codes to a list using the ListFromFile class
    private List<String> addNewCodes(String path){
        List<String> newList;
        listFromFile = new ListFromFile(getActivity().getApplicationContext());
        newList = listFromFile.readRewardsLine(path);
        for (String s : newList)
            Log.d(TAG, s);
        return newList;
    }

    //Adds the first set of reward codes to the database
    private void addInitialCodes(){
        List<String> startList;
        listFromFile = new ListFromFile(getActivity().getApplicationContext());
        startList = listFromFile.readRewardsLine("rewardcodes.txt");
        for (String s : startList)
            Log.d(TAG, s);

        myDb.storeRewards(startList);

        //'firstStart' boolean is set to false which means that the the method will not run after first
        //start
        SharedPreferences rewardsPrefs = getActivity().getSharedPreferences("rewardsPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = rewardsPrefs.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
    }

    //Method creating a custom Toast message
    public void toastMessage(String message){
        Toast.makeText(getActivity().getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }

    public void setTextToEdit(String textToEdit) {
        this.textToEdit = textToEdit;
    }

    public String getTextToEdit() {
        return textToEdit;
    }

    //Method that is used between fragments to update each other's points
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
