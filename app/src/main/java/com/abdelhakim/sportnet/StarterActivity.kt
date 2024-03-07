package com.abdelhakim.sportnet

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.abdelhakim.sportnet.authentication.SignInActivity
import com.abdelhakim.sportnet.databinding.ActivityStarterBinding
import com.abdelhakim.sportnet.fragments.ProfileFragment
import com.abdelhakim.sportnet.models.User
import com.abdelhakim.sportnet.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder

class StarterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStarterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStarterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference

        val currentUser = auth.currentUser
        if (currentUser != null) {
            getUserData(currentUser.uid)
        } else {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    private fun getUserData(uid: String) {
        database.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    MainActivity.currentUser = user // Assign user data to currentUser property
                    val gson = GsonBuilder().create()
                    MainActivity.userLikes = gson.fromJson(user.likes, Utils.arrayListOfStringsToken)
                }
                startMainActivity()

            }
            override fun onCancelled(error: DatabaseError) {
                // Error retrieving user data
                startMainActivity()
            }
        })
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

