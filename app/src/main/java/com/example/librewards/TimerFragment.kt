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
package com.example.librewards

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Chronometer.OnChronometerTickListener
import androidx.fragment.app.Fragment
import com.example.civice.R
import java.util.*

class TimerFragment : Fragment() {
    var popup: Dialog? = null
    var stopwatch: Chronometer? = null
    var myDb: DatabaseHelper? = null
    private var listFromFile: ListFromFile? = null
    var currStartCodes: MutableList<String> = ArrayList()
    var originalStartCodes: List<String> = ArrayList()
    var currStopCodes: MutableList<String> = ArrayList()
    var originalStopCodes: List<String> = ArrayList()
    private var editText: EditText? = null
    var textToEdit: String? = null
    private var startButton: Button? = null
    private var stopButton: Button? = null
    private var points: TextView? = null
    private var name: TextView? = null
    var listener: TimerListener? = null

    //Interface that consists of a method that will update the points in "RewardsFragment"
    interface TimerListener {
        fun onPointsTimerSent(points: Int)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_timer, container, false)
        //Assigns the field to the view's specified in the fragment_timer XML file file
        stopwatch = v.findViewById(R.id.stopwatch)
        editText = v.findViewById(R.id.startText)
        startButton = v.findViewById(R.id.startButton)
        stopButton = v.findViewById(R.id.stopButton)
        myDb = DatabaseHelper(activity!!.applicationContext)
        points = v.findViewById(R.id.points)
        points?.text = myDb?.myPoints.toString()
        name = v.findViewById(R.id.nameTimer)
        //Sets the name of user for this fragment by retrieving it from the database
        val wholeName = getString(R.string.Hey) + " " + myDb?.name
        name?.text = wholeName

        //Creating a preference for activity on first start-up only
        val timerPrefs = activity!!.getSharedPreferences("timerPrefs", Context.MODE_PRIVATE)
        val firstStart = timerPrefs.getBoolean("firstStart", true)
        //Anything enclosed in the 'if' statement will only run once; at first start-up.
        if (firstStart) {
            myDb!!.initialPoints()
            addInitialCodes()
        }

        //Gets all of the codes that are currently in the database and adds them to a list
        addCurrCodes(currStartCodes, "start_codes_table")
        addCurrCodes(currStopCodes, "stop_codes_table")
        //Gets all of the codes listed in the text files and add them to a list
        originalStartCodes = addNewCodes("startcodes.txt")
        originalStopCodes = addNewCodes("stopcodes.txt")
        //Checks if the text files have any codes different to the ones currently in the database and updates the
        //database if so. This is the method that would be used once the codes need to be refreshed. This
        //would happen every once in a while
        checkForUpdates(currStartCodes, originalStartCodes, "start_codes_table")
        checkForUpdates(currStopCodes, originalStopCodes, "stop_codes_table")
        //Sets actions on clicking the "Start" Button
        startButton?.setOnClickListener(View.OnClickListener {
            //Checks if there is any text inputted
            if (editText?.length() == 0) {
                toastMessage("No code was entered, please try again")
            } else if (currStartCodes.contains(editText?.text.toString())) {
                //Removes the code from the database as it has already been used once
                currStartCodes.remove(editText?.text.toString())
                myDb!!.deleteCode("start_codes_table", editText?.text.toString())
                //Clears the input text
                editText?.setText(null)
                editText?.hint = "Please enter the stop code"
                //Starts the stopwatch
                stopwatch?.base = SystemClock.elapsedRealtime()
                stopwatch?.start()
                //Switches from the 'Start' button to the 'Stop' button
                startButton?.visibility = View.INVISIBLE
                stopButton?.visibility = View.VISIBLE
                //All actions to be taken place once the stopwatch has started
                stopwatch?.onChronometerTickListener = OnChronometerTickListener {
                    //Checks if the stopwatch has gone over 24 hours. If so, the stopwatch resets back to its original state
                    if (SystemClock.elapsedRealtime() - stopwatch!!.base >= 800000) {
                        stopwatch?.base = SystemClock.elapsedRealtime()
                        stopwatch?.stop()
                        stopButton?.visibility = View.INVISIBLE
                        startButton?.visibility = View.VISIBLE
                        showPopup("No stop code was entered for 24 hours. The timer has been reset")
                    }
                    stopButton?.setOnClickListener(View.OnClickListener {
                        //Checks if there is any text inputted
                        if (editText?.length() == 0) {
                            toastMessage("No code was entered")
                        }
                        //Checks if the current stop code table in the database contains the code that has been inputted
                        if (currStopCodes.contains(editText?.text.toString())) {
                            //Removes the code from the database as it has already been used once
                            currStopCodes.remove(editText?.text.toString())
                            myDb?.deleteCode("stop_codes_table", editText?.text.toString())
                            //Clears the input text and resets to original state
                            editText?.setText(null)
                            editText?.hint = "Please enter the start code"
                            //'totalTime' gets the total duration spent at the library in milliseconds
                            val totalTime = SystemClock.elapsedRealtime() - stopwatch!!.base
                            //Sets the points using the setPoints method
                            setPointsFromTime(totalTime)
                            stopwatch?.setBase(SystemClock.elapsedRealtime())
                            stopwatch?.stop()
                            //Listener to communicate with Rewards Fragment and give the points to display in there
                            listener?.onPointsTimerSent(myDb!!.myPoints)
                            stopButton?.visibility = View.INVISIBLE
                            startButton?.visibility = View.VISIBLE
                        } else {
                            toastMessage("The code you entered is not valid, please try again")
                        }
                    })
                }
            } else {
                toastMessage(getString(R.string.invalidCode))
            }
        })
        return v
    }

    //Method that is used between fragments to update each other's points
    fun updatePoints(newPoints: Int) {
        points!!.text = newPoints.toString()
    }

    //Method to check if the text file has been updated with new codes or not
    fun checkForUpdates(currCodes: List<String>, originalCodes: List<String>, table: String?) {
        val tempCodes: MutableList<String> = ArrayList()
        //Loop to check if the elements in the 'currCodes' list exactly matches those in the text file. The ones that
        //match get added into a temporary list
        for (i in currCodes.indices) {
            for (j in originalCodes.indices) {
                if (originalCodes[j] == currCodes[i]) {
                    tempCodes.add(currCodes[i])
                }
            }
        }
        //Temporary list is compared with the current codes list. If they are not an
        //exact match, the codes update using the method in the DatabaseHelper class
        if (currCodes != tempCodes) {
            myDb!!.updateCodes(table, originalCodes)
        }
    }

    //Method to add the current codes that are in the database to a list
    private fun addCurrCodes(codeList: MutableList<String>, table: String) {
        val c = myDb!!.getAllData("codes", table)
        c.moveToFirst()
        while (!c.isAfterLast) {
            codeList.add(c.getString(c.getColumnIndex("codes")))
            c.moveToNext()
        }
    }

    //Method that converts the duration spent at the library into points
    fun setPointsFromTime(totalTime: Long) {
        var pointsEarned = 0
        val minutes = (totalTime / 1000 / 60).toInt()
        if (totalTime in 10001..29999) {
            pointsEarned = 10
            myDb?.addPoints(pointsEarned)
            points?.text = myDb?.myPoints.toString()
        } else if (totalTime in 30000..59999) {
            pointsEarned = 50
            myDb?.addPoints(pointsEarned)
            points?.text = myDb?.myPoints.toString()
        } else if (totalTime in 60000..119999) {
            pointsEarned = 75
            myDb?.addPoints(pointsEarned)
            points?.text = myDb?.myPoints.toString()
        } else if (totalTime in 120000..179999) {
            pointsEarned = 125
            myDb?.addPoints(pointsEarned)
            points?.text = myDb?.myPoints.toString()
        } else if (totalTime in 180000..259999) {
            pointsEarned = 225
            myDb?.addPoints(pointsEarned)
            points?.text = myDb?.myPoints.toString()
        } else if (totalTime in 260000..399999) {
            pointsEarned = 400
            myDb?.addPoints(pointsEarned)
            points?.text = myDb?.myPoints.toString()
        } else if (totalTime >= 500000) {
            pointsEarned = 700
            myDb?.addPoints(pointsEarned)
            points?.text = myDb?.myPoints.toString()
        }
        if (minutes == 1) {
            showPopup("""Well done, you spent $minutes minute at the library and have earned $pointsEarned points!
Your new points balance is: ${myDb?.myPoints}""")
        } else {
            showPopup("""Well done, you spent $minutes minutes at the library and have earned $pointsEarned points!
Your new points balance is: ${myDb?.myPoints}""")
        }
    }

    //Method to set the name on first start-up. Method is called in MainActivity
    fun initialSetName() {
        name?.text = getString(R.string.Hey) + " " + myDb?.name
    }


    //Method that creates a popup
    fun showPopup(text: String?) {
        popup = Dialog(activity!!)
        popup?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup?.setContentView(R.layout.popup_layout)
        val closeBtn = popup?.findViewById<ImageView>(R.id.closeBtn)
        val popupText = popup?.findViewById<TextView>(R.id.popupText)
        textToEdit = text
        popupText?.text = textToEdit
        closeBtn?.setOnClickListener { popup?.dismiss() }
        popup?.show()
    }

    //Method that adds the codes from the text file into a list using the ListFromFile class
    private fun addNewCodes(path: String): List<String> {
        listFromFile = ListFromFile(activity!!.applicationContext)
        val newList: List<String> = listFromFile!!.readLine(path)
        for (s in newList) Log.d(TAG, s)
        return newList
    }

    //Method adds codes to the database on first start-up
    private fun addInitialCodes() {
        listFromFile = ListFromFile(activity!!.applicationContext)
        val startList: List<String> = listFromFile!!.readLine("startcodes.txt")
        for (s in startList) Log.d(TAG, s)
        myDb!!.storeCodes(startList, "start_codes_table")
        val stopList: List<String> = listFromFile!!.readLine("stopcodes.txt")
        for (d in stopList) Log.d(TAG, d)
        myDb?.storeCodes(stopList, "stop_codes_table")

        //'firstStart' boolean is set to false which means that the the method will not run after first
        //start
        val timerPrefs = activity?.getSharedPreferences("timerPrefs", Context.MODE_PRIVATE)
        val editor = timerPrefs?.edit()
        editor?.putBoolean("firstStart", false)
        editor?.apply()
    }

    //Custom Toast message
    fun toastMessage(message: String?) {
        Toast.makeText(activity!!.applicationContext, message, Toast.LENGTH_LONG).show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is TimerListener) {
            context
        } else {
            throw RuntimeException(context.toString() + "must implement TimerListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        val TAG = TimerFragment::class.java.simpleName
    }
}