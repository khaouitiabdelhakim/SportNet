package com.abdelhakim.sportnet.authentication


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abdelhakim.sportnet.MainActivity
import com.abdelhakim.sportnet.R
import com.abdelhakim.sportnet.StarterActivity
import com.abdelhakim.sportnet.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.signIn.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signIn(email, password)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signUpIfAccountNo.setOnClickListener {
            // Navigate to sign up activity
            startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
            finish()
        }
    }

    private fun signIn(email: String, password: String) {
        binding.signInProgress.visibility = View.VISIBLE
        binding.signInText.visibility = View.INVISIBLE
        binding.signIn.isClickable = false
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show()
                    // Navigating to the main activity
                    startActivity(Intent(this@SignInActivity, StarterActivity::class.java))
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed. Please try again.",
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
            val intent = Intent(this, StarterActivity::class.java)
            startActivity(intent)
        }
    }
}
