package com.example.librewards

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.librewards.qrcode.QRCodeGenerator
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase


class Login : AppCompatActivity() {
    private lateinit var facebookFirstName : String
    private lateinit var facebookLastName: String
    private lateinit var facebookEmail: String
    private lateinit var facebookPhotoURL: String

    private lateinit var userFirstName: String
    private lateinit var userLastName: String
    private lateinit var userUniversity: String
    private lateinit var userEmail: String

    private lateinit var id: String
    private lateinit var loginButton: LoginButton
    private lateinit var callbackManager: CallbackManager
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var button: Button
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var registerHere: Button
    private lateinit var imageView: ImageView
    private lateinit var fh : FirebaseHandler
    private lateinit var qrGen : QRCodeGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (isLoggedIn()) {
            val mainActivity = MainActivity()
            getFacebookInfo(AccessToken.getCurrentAccessToken(), mainActivity)
        }
        auth = Firebase.auth
        loginButton = findViewById(R.id.login_button)
        button = findViewById(R.id.button)
        emailInput = findViewById(R.id.login_email)
        passwordInput = findViewById(R.id.login_password)
        registerHere = findViewById(R.id.register_here)
        imageView = findViewById(R.id.imageView3)

        FacebookSdk.sdkInitialize(this.applicationContext)
        AppEventsLogger.activateApp(application)
        callbackManager = CallbackManager.Factory.create()
        database = FirebaseDatabase.getInstance().reference
        fh = FirebaseHandler()
        qrGen = QRCodeGenerator()
        loginButton.setPermissions(listOf("public_profile ", "email"))

        registerHere.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        button.setOnClickListener {
            if (emailInput.text.toString() == "" || passwordInput.toString() == "") {
                Toast.makeText(baseContext, "Please ensure all fields are correctly filled out.", Toast.LENGTH_SHORT).show()
            } else {
                signIn()
            }
        }

        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("TAG", "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
                val facebookUniversity = FacebookUniversity()
                getFacebookInfo(loginResult.accessToken, facebookUniversity)

            }

            override fun onCancel() {
                Log.d("TAG", "Facebook onCancel.")
                Toast.makeText(this@Login, "Login Cancelled", Toast.LENGTH_LONG).show()
            }

            override fun onError(exception: FacebookException) {
                Log.d("TAG", "Facebook onError.")
                Toast.makeText(this@Login, exception.message, Toast.LENGTH_LONG).show()
            }
        })

    }
    private fun getUserLoginInfo(email : String, activity: AppCompatActivity){
       val id = fh.hashFunction(email)
        val refChild = database.child("universities").child(id)
        refChild .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(ds in dataSnapshot.children) {
                    userFirstName = ds.child("firstname").getValue(String::class.java).toString()
                    userLastName = ds.child("lastname").getValue(String::class.java).toString()
                    userUniversity = ds.child("university").getValue(String::class.java).toString()
                    userEmail = ds.child("email").getValue(String::class.java).toString()

                    Log.d("TAG", userFirstName + userLastName + userUniversity + userEmail)

                    val intent = Intent(this@Login, activity::class.java)
                    intent.putExtra("email", userEmail)
                    intent.putExtra("first_name", userFirstName)
                    intent.putExtra("last_name", userLastName)
                    intent.putExtra("university", userUniversity)
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })
    }

    private fun getFacebookInfo(token: AccessToken, activity: AppCompatActivity){
        val request = GraphRequest.newMeRequest(token) { `object`, response ->
            Log.d("TAG", `object`.toString())
            if (`object`.has("first_name")) {
               facebookFirstName = `object`.getString("first_name")
            }
            if (`object`.has("last_name")) {
                facebookLastName = `object`.getString("last_name")
            }
            if (`object`.has("email")) {
                facebookEmail = `object`.getString("email")
            }
            if(`object`.has("id")) {
                id = `object`.getString("id")
                facebookPhotoURL = "https://graph.facebook.com/$id/picture?type=normal"
            }
            if(AccessToken.getCurrentAccessToken() != null && !AccessToken.getCurrentAccessToken().isExpired){
                Log.d("TAG", "already logged in")
            }
            else {
                fh.writeNewUser(facebookEmail, facebookFirstName, facebookLastName, facebookEmail, "")
            }
            val intent = Intent(this@Login, activity::class.java)
            intent.putExtra("email", facebookEmail)
            intent.putExtra("first_name", facebookFirstName)
            intent.putExtra("last_name", facebookLastName)
            intent.putExtra("photo", facebookPhotoURL)

            startActivity(intent)
        }
        val parameters = Bundle()
        parameters.putString("fields", "id,first_name,last_name,email")
        request.parameters = parameters
        request.executeAsync()

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
        if (currentUser != null) {
            reload();
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun signIn() {
        auth.signInWithEmailAndPassword(emailInput.text.toString(), passwordInput.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithEmail:success")
                        val user = auth.currentUser
                        var isAdmin: String
                        val refChild = fh.getChild("users",emailInput.text.toString(),"admin")
                        refChild .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                isAdmin = dataSnapshot.value.toString()
                                if (isAdmin == "0") {
                                    val intent = Intent(this@Login, MainActivity::class.java)
                                    startActivity(intent)
                                } else if (isAdmin == "1") {

                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Failed to read value
                                Log.w("TAG", "Failed to read value.", error.toException())
                            }
                        })
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("TAG", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithCredential:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "signInWithCredential:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
    }

    private fun isLoggedIn(): Boolean {
        return AccessToken.getCurrentAccessToken() != null && !AccessToken.getCurrentAccessToken().isExpired
    }

    private fun updateUI(user: FirebaseUser?) {

    }

    private fun reload() {

    }

    fun login(view: View) {}

}