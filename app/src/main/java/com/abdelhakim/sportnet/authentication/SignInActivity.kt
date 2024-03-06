package com.abdelhakim.sportnet.authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.abdelhakim.sportnet.R
import com.abdelhakim.sportnet.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}