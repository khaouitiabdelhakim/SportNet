package com.abdelhakim.sportnet.editors

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
import com.abdelhakim.sportnet.authentication.SignUpActivity
import com.abdelhakim.sportnet.databinding.ActivityPostBinding
import com.abdelhakim.sportnet.databinding.ActivitySignUpBinding
import com.abdelhakim.sportnet.models.Post
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class PostActivity : AppCompatActivity() {

    lateinit var binding : ActivityPostBinding
    private lateinit var auth: FirebaseAuth

    private var selectedContentUri: Uri? = null

    companion object {
        private const val TAG = "ContentSelection"
        private const val PICK_IMAGE_REQUEST = 547
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        if(auth.currentUser != null ) {
            binding.username.text = MainActivity.currentUser.username
            Glide.with(this)
                .load(MainActivity.currentUser.profilePictureUrl)
                .apply(RequestOptions().centerCrop())
                .into(binding.profile)
        }

        binding.attachmentZone.visibility = View.INVISIBLE

        binding.video.setOnClickListener {
            openFilePicker("video")
        }

        binding.picture.setOnClickListener {
            openFilePicker("image")
        }


        binding.sharePost.setOnClickListener {
            val content = binding.text.text.toString()
            if (content.isNotEmpty() ) {
                sharePost(content)
            } else {
                Toast.makeText(this, "Please type something, a post cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        if(intent.getStringExtra("contentType").equals("video") || intent.getStringExtra("contentType").equals("image") ) {
            intent.getStringExtra("contentType")?.let { openFilePicker(it) }
        }

    }

    private fun openFilePicker(type: String) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "$type/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    selectedContentUri = data?.data
                    if (selectedContentUri == null) {
                        Log.e(TAG, "Selected Content URI is null")
                    }  else {
                        binding.attachmentZone.visibility = View.VISIBLE
                        binding.attachmentUrl.text = selectedContentUri.toString()
                    }
                }
            }
        }
    }

    private fun sharePost(content: String) {
        binding.sharePost.isClickable = false
        binding.sharePostText.visibility = View.INVISIBLE
        binding.sharePostProgress.visibility = View.VISIBLE
        // Check if there's selected content to upload
        if (selectedContentUri != null) {
            uploadContentAndSharePost(content)
        } else {
            createAndSharePost(content, null) // If no content, pass null
        }
    }

    private fun uploadContentAndSharePost(content: String) {
        // Show progress dialog or similar UI feedback
        // Upload selected content to Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference.child("posts/${UUID.randomUUID()}")
        val uploadTask = storageRef.putFile(selectedContentUri!!)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { downloadTask ->
            if (downloadTask.isSuccessful) {
                // Get the download URL of the uploaded content
                val contentUrl = downloadTask.result.toString()
                createAndSharePost(content, contentUrl)
            } else {
                // Handle failure to upload content
                Toast.makeText(this, "Failed to upload content", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createAndSharePost(content: String, contentUrl: String?) {
        val post = Post(
            id = FirebaseDatabase.getInstance().reference.child("posts").push().key ?: "",
            userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            userProfileUrl = MainActivity.currentUser.profilePictureUrl,
            username = MainActivity.currentUser.username,
            roomId = "", // You need to set the room ID
            text = content,
            contentUrl = contentUrl ?: "", // If no content URL, set empty string
            time = System.currentTimeMillis(),
            likes = 0,
            shares = 0,
            views = 0,
            comments = 0
        )

        // Save the post to the database
        val postRef = FirebaseDatabase.getInstance().reference.child("posts").child(post.id)
        postRef.setValue(post)
            .addOnSuccessListener {
                Toast.makeText(this, "Post shared successfully", Toast.LENGTH_SHORT).show()
                finish() // Finish the activity after sharing the post
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to share post", Toast.LENGTH_SHORT).show()
                binding.sharePost.isClickable = true
                binding.sharePostText.visibility = View.VISIBLE
                binding.sharePostProgress.visibility = View.INVISIBLE
            }
    }


}