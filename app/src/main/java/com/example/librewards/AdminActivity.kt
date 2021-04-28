package com.example.librewards

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.librewards.models.Universities
import com.google.android.gms.common.api.Response
import com.google.firebase.database.*
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.likethesalad.android.aaper.api.EnsurePermissions


class AdminActivity : AppCompatActivity() {
    private lateinit var fh : FirebaseHandler
    private lateinit var database : DatabaseReference
    private lateinit var timerFragment: TimerFragment

    private lateinit var cameraButton : Button
    private lateinit var results : TextView
    private lateinit var editText : EditText

    private lateinit var email : String
    private lateinit var firstName : String
    private lateinit var lastName : String
    private lateinit var university : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Sets the layout to the XML file associated with it
        setContentView(R.layout.activity_admin)

        cameraButton = findViewById(R.id.camera_button)
        editText = findViewById(R.id.editTextTextPersonName)
        database = FirebaseDatabase.getInstance().reference
        results = findViewById(R.id.results)
        timerFragment = TimerFragment()

       ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PackageManager.PERMISSION_GRANTED)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (intentResult != null) {
            if (intentResult.contents == null) {
                Toast.makeText(baseContext, "Cancelled", Toast.LENGTH_LONG).show()
            } else {

                results.text = intentResult.contents

                var isStudying : String
                val refChild = fh.getChild("users",intentResult.contents, "studying")
                refChild.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        isStudying = dataSnapshot.value.toString()
                        if (isStudying == "0" || isStudying == "2") {
                            refChild.setValue("1")
                        }
                        else if (isStudying == "1") {
                            refChild.setValue("0")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w("TAG", "Failed to read value.", error.toException())
                    }
                })
            }


            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    private fun initialiseVariables(){
        val extras = intent.extras
        email = extras?.getString("email").toString()
        firstName = extras?.getString("first_name").toString()
        lastName = extras?.getString("last_name").toString()
        university = extras?.getString("university").toString()
    }
    fun writeNewProduct(productName : String, cost : String) {
        val database = FirebaseDatabase.getInstance().reference
        val university = Universities()
        database.child("universities").child(id).setValue(user)
    }

    fun scanButton(view: View) {
        val intentIntegrator = IntentIntegrator(this)
        intentIntegrator.initiateScan()
    }

}
