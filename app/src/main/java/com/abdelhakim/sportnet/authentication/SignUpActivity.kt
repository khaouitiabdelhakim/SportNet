package com.abdelhakim.sportnet.authentication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abdelhakim.sportnet.MainActivity
import com.abdelhakim.sportnet.R
import com.abdelhakim.sportnet.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTheme(R.style.Theme_SportNet)

        auth = FirebaseAuth.getInstance()

        binding.signIn.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signUp(email, password)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signInIfAccountYes.setOnClickListener {
            // Navigate to sign in activity
            startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
            finish()
        }
    }

    private fun signUp(email: String, password: String) {
        binding.signInProgress.visibility = View.VISIBLE
        binding.signInText.visibility = View.INVISIBLE
        binding.signIn.isClickable = false
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up success, update UI with the signed-in user's information
                    Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()
                    // Navigating to the main activity
                    startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                } else {
                    // If sign up fails, display a message to the user.
                    Toast.makeText(baseContext, "Sign up failed. Try again.",
                        Toast.LENGTH_SHORT).show()

                    binding.signInProgress.visibility = View.INVISIBLE
                    binding.signInText.visibility = View.VISIBLE
                    binding.signIn.isClickable = true
                }
            }
    }



    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and email is verified, then update UI accordingly.
        val user = auth.currentUser
        if (user != null) {
            finish()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
