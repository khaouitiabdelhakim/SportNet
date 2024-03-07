package com.abdelhakim.sportnet.authentication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abdelhakim.sportnet.MainActivity
import com.abdelhakim.sportnet.R
import com.abdelhakim.sportnet.StarterActivity
import com.abdelhakim.sportnet.databinding.ActivitySignUpBinding
import com.abdelhakim.sportnet.models.User
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private var selectedThumbnailUri: Uri? = null

    companion object {
        private const val TAG = "ProfilePictureSelection"
        private const val PICK_IMAGE_REQUEST = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.signIn.setOnClickListener {
            val username = binding.username.text.toString()
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()) {
                signUp(email, password, username)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signInIfAccountYes.setOnClickListener {
            // Navigate to sign in activity
            startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
            finish()
        }

        binding.pictureUrl.setOnClickListener {
            openFilePicker()
        }
    }


    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    selectedThumbnailUri = data?.data
                    if (selectedThumbnailUri != null) {
                        Log.e(TAG, "Profile URI: $selectedThumbnailUri")
                        Glide.with(this)
                            .load(selectedThumbnailUri)
                            .apply(RequestOptions().placeholder(R.drawable.rounded_profile).centerCrop())
                            .into(binding.profilePictureUrl)
                    } else {
                        Log.e(TAG, "Selected Profile URI is null")
                    }
                }
            }
        }
    }

    private fun signUp(email: String, password: String, username:String) {
        binding.signInProgress.visibility = View.VISIBLE
        binding.signInText.visibility = View.INVISIBLE
        binding.signIn.isClickable = false
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        // Upload profile picture to Firebase Storage
                        selectedThumbnailUri?.let { uri ->
                            val storageRef = FirebaseStorage.getInstance().reference.child("profile_pictures/${user.uid}")
                            val uploadTask = storageRef.putFile(uri)

                            uploadTask.continueWithTask { task ->
                                if (!task.isSuccessful) {
                                    task.exception?.let {
                                        throw it
                                    }
                                }
                                storageRef.downloadUrl
                            }.addOnCompleteListener { downloadTask ->
                                if (downloadTask.isSuccessful) {
                                    val profilePictureUrl = downloadTask.result.toString()
                                    // Create a User object with required data
                                    val newUser = User(
                                        id = user.uid,
                                        email = email,
                                        username = username,
                                        profilePictureUrl = profilePictureUrl,
                                        likes = "[]" // Default likes value
                                    )
                                    // Save user data in Realtime Database
                                    FirebaseDatabase.getInstance().reference.child("users").child(user.uid).setValue(newUser)
                                        .addOnCompleteListener { databaseTask ->
                                            if (databaseTask.isSuccessful) {
                                                // Sign up success, update UI with the signed-in user's information
                                                Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()
                                                // Navigating to the main activity
                                                startActivity(Intent(this@SignUpActivity, StarterActivity::class.java))
                                            } else {
                                                Toast.makeText(this, "Failed to create user node in database", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } ?: run {
                            // If no profile picture selected, create user without profile picture URL
                            val newUser = User(
                                id = user.uid,
                                email = email,
                                username = username, // You need to get the username from somewhere
                                profilePictureUrl = "", // Empty profile picture URL
                                likes = "[]" // Default likes value
                            )
                            FirebaseDatabase.getInstance().reference.child("users").child(user.uid).setValue(newUser)
                                .addOnCompleteListener { databaseTask ->
                                    if (databaseTask.isSuccessful) {
                                        // Sign up success, update UI with the signed-in user's information
                                        Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()
                                        // Navigating to the main activity
                                        startActivity(Intent(this@SignUpActivity, StarterActivity::class.java))
                                    } else {
                                        Toast.makeText(this, "Failed to create user node in database", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                } else {
                    // If sign up fails, display a message to the user.
                    Toast.makeText(baseContext, "Sign up failed. Try again.", Toast.LENGTH_SHORT).show()

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
