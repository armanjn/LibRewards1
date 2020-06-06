package com.example.librewards;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;

import java.io.FileNotFoundException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TimerFragment.TimerListener, RewardsFragment.RewardsListener{
    DatabaseHelper myDb;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private TimerFragment timerFragment;
    private RewardsFragment rewardsFragment;
    EditText test;
    Button btnTest;
    TextView points;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDb = new DatabaseHelper(this);
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);


        timerFragment = new TimerFragment();
        rewardsFragment = new RewardsFragment();


        tabLayout.setupWithViewPager(viewPager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(timerFragment, "Timer");
        viewPagerAdapter.addFragment(rewardsFragment, "Rewards");
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.getTabAt(0).setIcon(R.drawable.timer);
        tabLayout.getTabAt(1).setIcon(R.drawable.reward);

         test = (EditText) findViewById(R.id.startText);
         btnTest = (Button) findViewById(R.id.startButton);



         SharedPreferences prefs = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
         boolean firstStart = prefs.getBoolean("firstStart", true);
         if (firstStart){
             addInitialPoints();
         }

         myDb.addPoints(40);


    }

    public void addInitialPoints(){
        myDb.initialPoints();
        SharedPreferences prefs = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
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
