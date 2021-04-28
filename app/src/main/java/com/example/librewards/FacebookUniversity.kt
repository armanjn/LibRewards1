package com.example.librewards

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


class FacebookUniversity : AppCompatActivity() {
    private lateinit var spinner : Spinner
    private var spinnerPos : Int? = null
    private lateinit var uniSelected: String
    private lateinit var localDb : DatabaseHandler
    private lateinit var button : Button
    private lateinit var login : Login
    private lateinit var fh : FirebaseHandler

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_facebook_university)
        spinner = findViewById(R.id.spinner2)
        button = findViewById(R.id.button2)
        localDb = DatabaseHandler(applicationContext)
        login = Login()
        fh = FirebaseHandler()

        storeUniversities()

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                spinnerPos = position
                if (position == 0) {
                    Log.d("TAG", "First element in spinner")
                }
                else {
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
            if(spinnerPos!=0){
                val extras = intent.extras
                val email = extras?.getString("fields")
                fh.getChild("users",email!!,"university").setValue(uniSelected)
            }
            else{
                Toast.makeText(baseContext, "Please ensure you have selected a University", Toast.LENGTH_SHORT).show()

            }
        }

    }

    private fun loadSpinnerData() {
        // Spinner Drop down elements
        val universities: MutableList<String> = localDb.getAllUniversities() as MutableList<String>
        universities.add(0, "Please choose a University")
        // Creating adapter for spinner
        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, universities)

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // attaching data adapter to spinner
        spinner.adapter = dataAdapter
    }

    private fun storeUniversities() {
        val listFromFile = ListFromFile(applicationContext)
        val uniList: List<String> = listFromFile.readLine("universities.txt")
        for (s in uniList) Log.d("letsSee", s)
        localDb.storeUniversities(uniList, "universities_table")

    }
}