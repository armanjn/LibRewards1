package com.example.librewards

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase


class Register : AppCompatActivity() {
    private lateinit var spinner: Spinner
    private lateinit var localDb: DatabaseHandler
    private lateinit var database: DatabaseReference
    private lateinit var button: Button
    private lateinit var firstName: EditText
    private lateinit var surname: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var uniSelected: String
    private lateinit var login : Button
    private var spinnerPos : Int? = null
    private lateinit var fh : FirebaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = Firebase.auth
        spinner = findViewById(R.id.spinner)
        localDb = DatabaseHandler(applicationContext)
        button = findViewById(R.id.button)
        login = findViewById(R.id.login)
        firstName = findViewById(R.id.registration_firstname)
        surname = findViewById(R.id.registration_surname)
        email = findViewById(R.id.registration_email)
        password = findViewById(R.id.registration_password)
        fh = FirebaseHandler()
        database = FirebaseDatabase.getInstance().reference

        storeUniversities()

        login.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                spinnerPos = position
                if (position == 0) {
                    Log.d("TAG","First element in spinner")
                } else {
                    // On selecting a spinner item
                    uniSelected = parent?.getItemAtPosition(position).toString()

                    // Showing selected spinner item
                    Toast.makeText(parent?.context, "You selected: $uniSelected",
                            Toast.LENGTH_LONG).show()

                }
            }
        }

        loadSpinnerData()

        button.setOnClickListener {
            if (email.text.toString() == "" || password.toString() == "" || firstName.toString() == "" || surname.text.toString() == "" || spinnerPos==0) {
                Toast.makeText(baseContext, "Please ensure all fields are correctly filled out.", Toast.LENGTH_SHORT).show()
            } else{
                signUp()
            }
        }

    }

    private fun signUp() {
        auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        fh.writeNewUser(email.text.toString(), firstName.text.toString(), surname.text.toString(), email.text.toString(), uniSelected)
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "createUserWithEmail:success")
                        val user = auth.currentUser
                        updateUI(user)
                        val intent = Intent(this, Login::class.java)
                        startActivity(intent)

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "createUserWithEmail:failure", task.exception)

                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
    }

    private fun loadSpinnerData() {
        // Spinner Drop down elements
        val universities: MutableList<String> = localDb.getAllUniversities() as MutableList<String>
        universities.add(0,"Please choose a University")
        // Creating adapter for spinner
        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, universities)
        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // attaching data adapter to spinner
        spinner.adapter = dataAdapter
    }

    private fun storeUniversities() {
        val listFromFile = ListFromFile(applicationContext)
        val uniList: List<String> = listFromFile.readLine("universities.txt")
        for (s in uniList) Log.d("TAG", s)
        localDb.storeUniversities(uniList, "universities_table")

    }

    private fun updateUI(user: FirebaseUser?) {

    }
}