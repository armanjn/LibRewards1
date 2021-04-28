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
import com.google.firebase.database.*
import java.util.*

class TimerFragment : Fragment() {
    var popup: Dialog? = null
    var stopwatch: Chronometer? = null
    var myDb: DatabaseHelper? = null
    private var editText: EditText? = null
    var textToEdit: String? = null
    private var totalTime: Long? = null
    private var startButton: Button? = null
    private var stopButton: Button? = null
    private var points: TextView? = null
    private var name: TextView? = null
    var listener: TimerListener? = null
    lateinit var database: DatabaseReference
    lateinit var fh: FirebaseHandler
    private lateinit var mainActivity: MainActivity
    private lateinit var adminActivity: AdminActivity

    //Interface that consists of a method that will update the points in "RewardsFragment"
    interface TimerListener {
        fun onPointsTimerSent(points: Int)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_timer, container, false)
        //Assigns the field to the view's specified in the fragment_timer XML file file
        mainActivity = activity as MainActivity
        adminActivity = AdminActivity()

        stopwatch = v.findViewById(R.id.stopwatch)
        editText = v.findViewById(R.id.startText)
        startButton = v.findViewById(R.id.startButton)
        stopButton = v.findViewById(R.id.stopButton)
        myDb = DatabaseHelper(requireActivity().applicationContext)
        points = v.findViewById(R.id.points)
        points?.text = myDb?.myPoints.toString()
        name = v.findViewById(R.id.nameTimer)
        fh = FirebaseHandler()
        database = FirebaseDatabase.getInstance().reference
        //Sets the name of user for this fragment by retrieving it from the database
        val wholeName = getString(R.string.Hey) + " " + myDb?.name
        name?.text = wholeName

        //Creating a preference for activity on first start-up only

        //Anything enclosed in the 'if' statement will only run once; at first start-up.
        if (firstStart()) {
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addTimerEventListener(database)

    }

    private fun addTimerEventListener(timerReference: DatabaseReference) {
        val refChild = fh.getChild("users",mainActivity.email, "studying")
        var isStudying: String
        val timerListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                isStudying = dataSnapshot.value.toString()
                Log.d("TAG", isStudying)
                if (isStudying == "1") {
                    stopwatch?.base = SystemClock.elapsedRealtime()
                    stopwatch?.start()
                    stopwatch?.onChronometerTickListener = OnChronometerTickListener {
                        //Checks if the stopwatch has gone over 24 hours. If so, the stopwatch resets back to its original state
                        if (SystemClock.elapsedRealtime() - stopwatch!!.base >= 800000) {
                            stopwatch?.base = SystemClock.elapsedRealtime()
                            stopwatch?.stop()
                            stopButton?.visibility = View.INVISIBLE
                            startButton?.visibility = View.VISIBLE
                            showPopup("No stop code was entered for 24 hours. The timer has been reset")
                        }

                    }

                } else if (isStudying == "0") {
                    totalTime = SystemClock.elapsedRealtime() - stopwatch!!.base
                    setPointsFromTime(totalTime!!)
                    stopwatch?.base = SystemClock.elapsedRealtime()
                    stopwatch?.stop()
                    //Listener to communicate with Rewards Fragment and give the points to display in there
                    listener?.onPointsTimerSent(myDb!!.myPoints)
                    refChild.setValue("2")
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        }
        refChild.addValueEventListener(timerListener)
    }

    //Method that converts the duration spent at the library into points
    private fun setPointsFromTime(totalTime: Long) {
        val minutes = (totalTime / 1000 / 60).toInt()
        val pointsEarned: Int = when (totalTime) {
            in 0..10000 -> 0
            in 10001..29999 -> 10
            in 30000..59999 -> 50
            in 60000..119999 -> 75
            in 120000..179999 -> 125
            in 180000..259999 -> 225
            in 260000..399999 -> 400
            else -> 700
        }
        myDb?.addPoints(pointsEarned)
        points?.text = myDb?.myPoints.toString()
        if (minutes == 1) {
            showPopup("Well done, you spent $minutes minute at the library and have earned $pointsEarned points! Your new points balance is: ${myDb?.myPoints}")
        } else {
            showPopup("Well done, you spent $minutes minutes at the library and have earned $pointsEarned points! Your new points balance is:  ${myDb?.myPoints}")
        }
    }

    private fun firstStart(): Boolean {
        val timerPrefs = requireActivity().getSharedPreferences("timerPrefs", Context.MODE_PRIVATE)
        return timerPrefs.getBoolean("firstStart", true)
    }


    //Method to set the name on first start-up. Method is called in MainActivity
    fun initialSetName() {
        name?.text = getString(R.string.Hey) + " " + myDb?.name
    }

    fun updatePoints(newPoints: Int) {
        points!!.text = newPoints.toString()
    }

    //Method that creates a popup
    private fun showPopup(text: String?) {
        popup = Dialog(requireActivity())
        popup?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup?.setContentView(R.layout.popup_layout)
        val closeBtn = popup?.findViewById<ImageView>(R.id.closeBtn)
        val popupText = popup?.findViewById<TextView>(R.id.popupText)
        textToEdit = text
        popupText?.text = textToEdit
        closeBtn?.setOnClickListener { popup?.dismiss() }
        popup?.show()
    }

    //Custom Toast message
    fun toastMessage(message: String?) {
        Toast.makeText(requireActivity().applicationContext, message, Toast.LENGTH_LONG).show()
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