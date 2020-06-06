package com.example.librewards;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class RewardsFragment extends Fragment {
    Dialog popup;
    DatabaseHelper myDb;

    private EditText editText;
    private TextView points;
    private Button rewardMe;

    RewardsFragment.RewardsListener listener;

    public interface RewardsListener {
        void onPointsRewardsSent(int points);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_rewards, container, false);

        myDb = new DatabaseHelper(getActivity().getApplicationContext());
        points = v.findViewById(R.id.points2);
        points.setText(String.valueOf(myDb.getPoints()));
        // Inflate the layout for this fragment
        return v;
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
