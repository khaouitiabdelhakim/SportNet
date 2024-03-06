package com.abdelhakim.sportnet

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.abdelhakim.sportnet.databinding.ActivityStarterBinding

class StarterActivity : AppCompatActivity() {

    lateinit var binding: ActivityStarterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStarterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler().postDelayed({
            // Start the MainActivity
            startActivity(Intent(this@StarterActivity, MainActivity::class.java))
            finish() // Finish the StarterActivity so it won't appear again when back button pressed
        }, 2000) // Wait for 2 seconds (2000 milliseconds)
    }
}
