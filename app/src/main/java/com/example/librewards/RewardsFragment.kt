/*Author: Arman Jalilian
Date of Completion: 07/06/2020
Module Code: CSC3122
Application Name: Lib Rewards
Application Purpose: Rewards students as they spend time at the library
Class Name: RewardsFragment
Class Purpose: The rewards fragment is where a user can spend the points that they have earned. They can acquire a code from the library shop which will decrease the amount of
points, if they have enough. This will prove to the library staff that they have spent the points and therefore they can in return reward them.
 */
package com.example.librewards

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.librewards.models.Universities
import com.example.librewards.models.User
import com.example.librewards.qrcode.QRCodeGenerator
import com.google.common.hash.Hashing
import com.google.firebase.database.*
import java.io.IOException
import java.io.InputStream
import java.lang.Error
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class RewardsFragment : Fragment() {
    private lateinit var popup: Dialog
    private lateinit var myDb: DatabaseHelper
    private lateinit var listFromFile: ListFromFile
    private lateinit var editText: EditText
    private lateinit var points: TextView
    private lateinit var name: TextView
    private lateinit var rewardButton: Button
    private var rewardsCodes: List<String> = ArrayList()
    private var listener: RewardsListener? = null
    private lateinit var textToEdit: String
    private lateinit var fh : FirebaseHandler
    private lateinit var mainActivity : MainActivity

    //Interface that consists of a method that will update the points in "TimerFragment"
    interface RewardsListener {
        fun onPointsRewardsSent(points: Int)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_rewards, container, false)
        //Assigns the field to the view's specified in the fragment_timer XML file file
        mainActivity = activity as MainActivity
        myDb = DatabaseHelper(requireActivity().applicationContext)
        rewardButton = v.findViewById(R.id.rewardButton)
        editText = v.findViewById(R.id.rewardText)
        points = v.findViewById(R.id.points2)
        points.text = myDb.myPoints.toString()
        name = v.findViewById(R.id.nameRewards)
        fh = FirebaseHandler()


        //Sets the name of user for this fragment by retrieving it from the database
        val wholeName = getString(R.string.Hey) + " " + myDb.name
        name.text = wholeName

        //Creating a preference for activity on first start-up only
        val rewardsPrefs = mainActivity.getSharedPreferences("rewardsPrefs", Context.MODE_PRIVATE)
        val firstStart = rewardsPrefs.getBoolean("firstStart", true)
        //Anything enclosed in the 'if' statement will only run once; at first start-up.
        if (firstStart) {
            addInitialCodes()
        }
        //Adds the codes from the text file to the database and updates the database every time in case there are new codes or costs in the text file
        rewardsCodes = addNewCodes("rewardcodes.txt")
        myDb.updateRewardCodes(rewardsCodes)

        //Sets actions on clicking the "Reward" Button
        rewardButton.setOnClickListener(View.OnClickListener {
            //Checks if the input text is empty
            if (editText.length() == 0) {
                toastMessage("No code was entered, please try again")
            } else if (rewardsCodes.contains(editText.text.toString())) {
                //Checks if the cost that comes with the code is greater than the points a user has
                if (myDb.myPoints > myDb.getCost(editText.text.toString())) {
                    //If the user has enough points, the cost is deducted from the points and the user can get their reward
                    myDb.minusPoints(myDb.getCost(editText.text.toString()))
                    showPopup("Code accepted, keep it up! Your new points balance is: " + myDb.myPoints)
                    points.text = myDb.myPoints.toString()
                    //Communicates the new point balance with other fragment
                    listener?.onPointsRewardsSent(myDb.myPoints)
                } else {
                    showPopup(getString(R.string.insufficientFunds))
                }
            } else {
                toastMessage(getString(R.string.invalidCode))
            }
        })
        return v
    }

    private fun addRewardEventListener(timerReference: DatabaseReference) {
        val refChild = fh.getChild("university_rewards",mainActivity.email, "studying")
        var isStudying: String
        val timerListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                isStudying = dataSnapshot.value.toString()
                Log.d("TAG", isStudying)


            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        }
        refChild.addValueEventListener(timerListener)
    }



    //Method that creates a custom popup
    private fun showPopup(text: String) {
        popup = Dialog(requireActivity())
        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup.setContentView(R.layout.popup_layout)
        val closeBtn = popup.findViewById<ImageView>(R.id.closeBtn)
        val popupText = popup.findViewById<TextView>(R.id.popupText)
        textToEdit = text
        popupText.text = textToEdit
        closeBtn.setOnClickListener { popup.dismiss() }
        popup.show()
    }

    //Method to set the name on first start-up. Method is called in MainActivity
    fun initialSetName() {
        name.text = getString(R.string.Hey) + " " + myDb.name
    }

    //Method that adds new reward codes to a list using the ListFromFile class
    private fun addNewCodes(path: String): List<String> {
        listFromFile = ListFromFile(requireActivity().applicationContext)
        val newList: List<String> = listFromFile.readRewardsLine(path)
        for (s in newList) Log.d(TAG, s)
        return newList
    }

    //Adds the first set of reward codes to the database
    private fun addInitialCodes() {
        listFromFile = ListFromFile(requireActivity().applicationContext)
        val startList: List<String> = listFromFile.readRewardsLine("rewardcodes.txt")
        for (s in startList) Log.d(TAG, s)
        myDb.storeRewards(startList)

        //'firstStart' boolean is set to false which means that the the method will not run after first
        //start
        val rewardsPrefs = requireActivity().getSharedPreferences("rewardsPrefs", Context.MODE_PRIVATE)
        val editor = rewardsPrefs.edit()
        editor.putBoolean("firstStart", false)
        editor.apply()
    }

    //Method creating a custom Toast message
    private fun toastMessage(message: String) {
        Toast.makeText(requireActivity().applicationContext, message, Toast.LENGTH_LONG).show()
    }

    //Method that is used between fragments to update each other's points
    fun updatedPoints(newPoints: Int) {
        points.text = newPoints.toString()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is RewardsListener) {
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
        private val TAG = RewardsFragment::class.java.simpleName
    }
}