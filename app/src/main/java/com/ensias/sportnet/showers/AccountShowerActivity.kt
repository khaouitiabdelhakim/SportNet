package com.ensias.sportnet.showers

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ensias.sportnet.adapters.PostAdapter
import com.ensias.sportnet.databinding.ActivityAccountShowerBinding
import com.ensias.sportnet.models.Post
import com.ensias.sportnet.models.User
import com.ensias.sportnet.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ensias.sportnet.MainActivity
import com.ensias.sportnet.adapters.OwnPostAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AccountShowerActivity : AppCompatActivity() {

    lateinit var binding: ActivityAccountShowerBinding
    lateinit var adapter: OwnPostAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    companion object {
        lateinit var currentAccount: User
        var posts = ArrayList<Post>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountShowerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        posts.clear()

        binding.postsRecyclerView.layoutManager = LinearLayoutManager(this@AccountShowerActivity, LinearLayoutManager.VERTICAL, false)
        adapter = OwnPostAdapter(this, posts)
        binding.postsRecyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        loadUserPostsAndInfo()
        binding.editProfile.visibility = View.GONE

        binding.editProfile.setOnClickListener {
            val intent = Intent(this@AccountShowerActivity,ProfileActivity::class.java)
            intent.putExtra("userId", currentAccount.id)
            startActivity(intent)
        }
    }

    private fun loadUserPostsAndInfo() {
        val userId = intent.getStringExtra("accountId")
        val userReference = database.getReference("users").child(userId.toString())

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    currentAccount = it
                    // Set user information
                    binding.profileUsername.text = currentAccount.username
                    binding.profileDescription.text = currentAccount.description
                    binding.profileDate.text = ". since ${Utils.formatDate(currentAccount.createdAt)}"

                    Glide.with(this@AccountShowerActivity)
                        .load(currentAccount.profilePictureUrl)
                        .apply(RequestOptions().centerCrop())
                        .into(binding.profilePicture)

                    Glide.with(this@AccountShowerActivity)
                        .load(currentAccount.profilePictureUrl)
                        .apply(RequestOptions().centerCrop())
                        .into(binding.profileBanner)

                    if (currentAccount.username.equals(MainActivity.currentUser.username)) binding.editProfile.visibility = View.VISIBLE


                }

                // Load user's posts
                loadUserPosts(userId)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                binding.loadingProgress.visibility = View.GONE
                binding.result.visibility = View.VISIBLE
            }
        })
    }

    private fun loadUserPosts(userId: String?) {
        userId?.let {
            val postsReference = database.getReference("posts")

            // Check if userId is not null before executing the query
            val query = postsReference.orderByChild("userId").equalTo(userId)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    posts.clear()
                    for (postSnapshot in snapshot.children) {
                        val post = postSnapshot.getValue(Post::class.java)
                        post?.let {
                            posts.add(it)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    binding.loadingProgress.visibility = View.GONE
                    binding.result.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    Log.e("Firebase", "Error loading user posts: ${error.message}")
                    binding.loadingProgress.visibility = View.GONE
                    binding.result.visibility = View.VISIBLE
                }
            })
        } ?: run {
            // Handle case when userId is null
            Log.e("Firebase", "userId is null")
            binding.loadingProgress.visibility = View.GONE
            binding.result.visibility = View.VISIBLE
        }
    }

}
