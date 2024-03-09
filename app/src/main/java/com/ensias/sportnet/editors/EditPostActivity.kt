package com.ensias.sportnet.editors

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ensias.sportnet.MainActivity
import com.ensias.sportnet.databinding.ActivityEditPostBinding
import com.ensias.sportnet.models.Post
import com.ensias.sportnet.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPostBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var postRef: DatabaseReference
    private lateinit var currentPost: Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        postRef = database.reference.child("posts").child(intent.getStringExtra("postId").toString())

        if(auth.currentUser != null ) {
            binding.username.text = MainActivity.currentUser.username
            Glide.with(this)
                .load(MainActivity.currentUser.profilePictureUrl)
                .apply(RequestOptions().centerCrop())
                .into(binding.profile)
            binding.date.text = Utils.formatDate(MainActivity.currentUser.createdAt)
        }

        loadCurrentPost()

        binding.updateProfile.setOnClickListener {
            updatePost()
        }
    }

    private fun loadCurrentPost() {
        postRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                currentPost = snapshot.getValue(Post::class.java)!!
                binding.text.setText(currentPost.text)
            }
        }.addOnFailureListener { exception ->
            Log.e("EditPostActivity", "Error fetching current post: ${exception.message}")
        }
    }

    private fun updatePost() {
        val newText = binding.text.text.toString()
        if (newText != currentPost.text) {
            binding.updateProfile.isClickable = false
            binding.updateProfileText.visibility = View.INVISIBLE
            binding.updateProfileProgress.visibility = View.VISIBLE

            currentPost.text = newText
            postRef.setValue(currentPost)
                .addOnSuccessListener {
                    Toast.makeText(this, "Post updated successfully", Toast.LENGTH_SHORT).show()
                    updateUIAfterUpdate()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to update post: ${exception.message}", Toast.LENGTH_SHORT).show()
                    updateUIAfterUpdate()
                }
        } else {
            Toast.makeText(this, "No changes to update", Toast.LENGTH_SHORT).show()
            updateUIAfterUpdate()
        }
    }

    private fun updateUIAfterUpdate() {
        binding.updateProfile.isClickable = true
        binding.updateProfileText.visibility = View.VISIBLE
        binding.updateProfileProgress.visibility = View.INVISIBLE
    }
}
