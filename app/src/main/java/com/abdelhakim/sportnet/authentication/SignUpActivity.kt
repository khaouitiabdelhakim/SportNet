package com.abdelhakim.sportnet.authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.abdelhakim.sportnet.R
import com.abdelhakim.sportnet.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}