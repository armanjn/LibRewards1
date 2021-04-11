/*Author: Arman Jalilian
Date of Completion: 07/06/2020
Module Code: CSC3122
Application Name: Lib Rewards
Application Purpose: Rewards students as they spend time at the library
Class Name: MainActivity
Class Purpose: The main activity is where the fragments are called within. It is also where the navigation is called and set from.
 */
package com.example.librewards

import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.civice.R
import com.example.librewards.RewardsFragment.RewardsListener
import com.example.librewards.TimerFragment.TimerListener
import com.google.android.material.tabs.TabLayout
import java.util.*

class MainActivity : AppCompatActivity(), TimerListener, RewardsListener {
    lateinit var timerFragment: TimerFragment
    lateinit var rewardsFragment: RewardsFragment
    lateinit var myDb: DatabaseHelper
    lateinit var popup: Dialog
    lateinit var textToEdit: String
    lateinit var enterName: EditText
    lateinit var nameButton: Button
    lateinit var helpButton: ImageView
    lateinit var popupNameContainer: FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Sets the layout to the XML file associated with it
        setContentView(R.layout.activity_main)
        //Assigns the field to the view's specified in the fragment_timer XML file file
        myDb = DatabaseHelper(this)
        val viewPager: ViewPager = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        helpButton = findViewById(R.id.helpButton)
        enterName = findViewById(R.id.enterName)
        nameButton = findViewById(R.id.nameButton)
        popupNameContainer = findViewById(R.id.popupNameContainer)
        popupNameContainer.visibility = View.INVISIBLE
        timerFragment = TimerFragment()
        rewardsFragment = RewardsFragment()
        tabLayout.setupWithViewPager(viewPager)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, 0)
        viewPagerAdapter.addFragment(timerFragment, "Timer")
        viewPagerAdapter.addFragment(rewardsFragment, "Rewards")
        viewPager.adapter = viewPagerAdapter
        tabLayout.getTabAt(0)?.setIcon(R.drawable.timer)
        tabLayout.getTabAt(1)?.setIcon(R.drawable.reward)

        //Creating a preference for activity on first start-up only
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        //Anything enclosed in the 'if' statement will only run once; at first start-up. For this instance I only needed the application to set the name of the user once.
        val firstStart = prefs.getBoolean("firstStart", true)
        if (firstStart) {
            showPopupName()
        }
        //Help button on standby in case a user required information about the application
        helpButton.setOnClickListener(View.OnClickListener { showPopup(getString(R.string.helpInfo)) })
    }

    //Custom popup that asks for the users name on first start-up
    fun showPopupName() {
        popupNameContainer.visibility = View.VISIBLE
        nameButton.setOnClickListener {
            if (enterName.length() != 0) {
                //Adds the name given to the database
                myDb.addName(enterName.text.toString())
                popupNameContainer.visibility = View.INVISIBLE
                //Sets the names in the fragments instantly as they will be the first ones on show once the popup dismisses
                timerFragment.initialSetName()
                rewardsFragment.initialSetName()
                //Once the popup closes the "Help" popup opens to give the user information before they start
                showPopup(getString(R.string.helpInfo))
            } else {
                toastMessage("No name was entered, please try again")
            }
        }
        //Sets the 'firstStart' boolean to false so it won't be called again on the user's device
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean("firstStart", false)
        editor.apply()
    }

    //Method that creates a popup
    fun showPopup(text: String) {
        popup = Dialog(this)
        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup.setContentView(R.layout.popup_layout)
        val closeBtn = popup.findViewById<ImageView>(R.id.closeBtn)
        val popupText = popup.findViewById<TextView>(R.id.popupText)
        textToEdit = text
        popupText.text = textToEdit
        closeBtn.setOnClickListener { popup.dismiss() }
        popup.show()
    }

    fun toastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    //Using the interface in both fragments, the main activity is able to facilitate communication between the two fragments. Here, it sets the points in each fragment each time
    //it's updated
    override fun onPointsRewardsSent(points: Int) {
        timerFragment.updatePoints(points)
    }

    override fun onPointsTimerSent(points: Int) {
        rewardsFragment.updatedPoints(points)
    }

    //Using a tab layout tutorial from YouTube user @Coding In Flow, I was able to create a tab layout and customise it so it fit my theme.
    private inner class ViewPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentPagerAdapter(fm, behavior) {
        private val fragments: MutableList<Fragment> = ArrayList()
        private val fragmentTitle: MutableList<String> = ArrayList()
        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            fragmentTitle.add(title)
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitle[position]
        }
    }
}