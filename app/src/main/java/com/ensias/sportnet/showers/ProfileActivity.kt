package com.ensias.sportnet.showers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ensias.sportnet.databinding.ActivityProfileBinding
import com.ensias.sportnet.models.User
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var userRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private lateinit var currentUser: User
    private var selectedContentUri: Uri? = null

    private val getContent: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedContentUri = uri
                    Glide.with(this)
                        .load(selectedContentUri)
                        .apply(RequestOptions().centerCrop())
                        .into(binding.profile)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        userRef = database.reference.child("users").child(intent.getStringExtra("userId").toString())
        storageRef = storage.reference.child("profile_pictures").child(auth.currentUser!!.uid)

        loadCurrentUser()

        binding.updateProfile.setOnClickListener {
            updateProfile()
        }

        binding.profile.setOnClickListener {
            openFilePicker()
        }
    }

    private fun loadCurrentUser() {
        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                currentUser = snapshot.getValue(User::class.java)!!
                with(binding) {
                    username.text = currentUser.username
                    email.text = currentUser.email
                    description.setText(currentUser.description)
                    Glide.with(this@ProfileActivity)
                        .load(currentUser.profilePictureUrl)
                        .apply(RequestOptions().circleCrop())
                        .into(profile)
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("ProfileActivity", "Error fetching current user: ${exception.message}")
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        getContent.launch(intent)
    }

    private fun updateProfile() {
        val newDescription = binding.description.text.toString().trim()
        if (newDescription.isEmpty() && selectedContentUri == null) return

        binding.updateProfile.isClickable = false
        binding.updateProfileText.visibility = View.INVISIBLE
        binding.updateProfileProgress.visibility = View.VISIBLE

        if (newDescription.isNotEmpty()) {
            currentUser.description = newDescription
            updateUser()
        } else {
            selectedContentUri?.let {
                uploadProfilePicture(it)
            }
        }
    }

    private fun updateUser() {
        userRef.setValue(currentUser)
            .addOnSuccessListener {
                Log.d("ProfileActivity", "Profile description updated successfully")
                selectedContentUri?.let {
                    uploadProfilePicture(it)
                } ?: updateUIAfterUpdate()
            }
            .addOnFailureListener { exception ->
                handleProfileUpdateError(exception)
            }
    }

    private fun uploadProfilePicture(uri: Uri) {
        storageRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        currentUser.profilePictureUrl = downloadUri.toString()
                        updateUser()
                    }
                    .addOnFailureListener { exception ->
                        handleProfileUpdateError(exception)
                    }
            }
            .addOnFailureListener { exception ->
                handleProfileUpdateError(exception)
            }
    }

    private fun updateUIAfterUpdate() {
        Log.d("ProfileActivity", "Profile picture updated successfully")
        binding.updateProfile.isClickable = true
        binding.updateProfileText.visibility = View.VISIBLE
        binding.updateProfileProgress.visibility = View.INVISIBLE
    }

    private fun handleProfileUpdateError(exception: Exception) {
        Log.e("ProfileActivity", "Error updating profile: ${exception.message}")
        binding.updateProfile.isClickable = true
        binding.updateProfileText.visibility = View.VISIBLE
        binding.updateProfileProgress.visibility = View.INVISIBLE
    }
}