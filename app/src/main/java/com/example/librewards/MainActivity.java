package com.example.librewards;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TimerFragment.TimerListener, RewardsFragment.RewardsListener{
    DatabaseHelper myDb;
    Dialog popup;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private TimerFragment timerFragment;
    private RewardsFragment rewardsFragment;
    TextView points;
    private String textToEdit;
    private ImageView helpButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDb = new DatabaseHelper(this);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        helpButton = findViewById(R.id.helpButton);

        timerFragment = new TimerFragment();
        rewardsFragment = new RewardsFragment();


        tabLayout.setupWithViewPager(viewPager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(timerFragment, "Timer");
        viewPagerAdapter.addFragment(rewardsFragment, "Rewards");
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.getTabAt(0).setIcon(R.drawable.timer);
        tabLayout.getTabAt(1).setIcon(R.drawable.reward);


         SharedPreferences prefs = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
         boolean firstStart = prefs.getBoolean("firstStart", true);
         if (firstStart){
             addInitialPoints();
         }

         helpButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 showPopup(getString(R.string.helpInfo));
             }
         });



         myDb.addPoints(40);


    }

    public void addInitialPoints(){
        myDb.initialPoints();
        SharedPreferences prefs = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
    }

    public void showPopup(String text){
        popup = new Dialog(this);
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
    public void setTextToEdit(String textToEdit) {
        this.textToEdit = textToEdit;
    }

    public String getTextToEdit() {
        return textToEdit;
    }


    @Override
    public void onPointsRewardsSent(int points) {
        timerFragment.updatePoints(points);
    }

    @Override
    public void onPointsTimerSent(int points) {
        rewardsFragment.updatedPoints(points);
    }


    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<>();
        private List<String> fragmentTitle = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            fragmentTitle.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitle.get(position);
        }
    }
}
