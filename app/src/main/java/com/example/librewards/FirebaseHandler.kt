package com.example.librewards

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.librewards.models.User
import com.google.common.hash.Hashing

import com.google.firebase.database.*
import java.nio.charset.StandardCharsets

class FirebaseHandler : AppCompatActivity() {

    fun writeNewUser(emailId: String, firstName: String, surname: String, email: String, university: String) {
        val database = FirebaseDatabase.getInstance().reference
        val user = User(firstName, surname, email, university, "0", "0")
        val id = Hashing.sipHash24().hashString(emailId, StandardCharsets.UTF_8)
                .toString()
        database.child("users").child(id).setValue(user)
    }

    fun getChild(document : String, email : String,path : String) : DatabaseReference{
        val database = FirebaseDatabase.getInstance().reference
        val id = Hashing.sipHash24().hashString(email, StandardCharsets.UTF_8)
                .toString()
        return database.child(document).child(id).child(path)
    }

    fun addRewardProduct(item : String, price : Int){

    }

    fun hashFunction(email : String) : String{
        return Hashing.sipHash24().hashString(email, StandardCharsets.UTF_8)
                .toString()
    }

}