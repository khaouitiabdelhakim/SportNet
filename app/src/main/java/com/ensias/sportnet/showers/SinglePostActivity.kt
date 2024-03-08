package com.ensias.sportnet.showers

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ensias.sportnet.MainActivity.Companion.userLikes
import com.ensias.sportnet.R
import com.ensias.sportnet.authentication.SignInActivity
import com.ensias.sportnet.databinding.ActivitySinglePostBinding
import com.ensias.sportnet.editors.CommentsActivity
import com.ensias.sportnet.models.Post
import com.ensias.sportnet.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.gson.GsonBuilder
import java.util.regex.Matcher
import java.util.regex.Pattern

class SinglePostActivity : AppCompatActivity() {

    lateinit var binding: ActivitySinglePostBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    lateinit var currentPost: Post
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySinglePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        loadPost()

        binding.username.setOnClickListener {
            val intent = Intent(this, AccountShowerActivity::class.java)
            intent.putExtra("accountId",currentPost.userId)
            ContextCompat.startActivity(this, intent, null)
        }
    }

    private fun loadPost() {
        val postId = intent.getStringExtra("postId")
        val user = auth.currentUser

        val categoryReference = database.getReference("posts/$postId")

        categoryReference.get()
            .addOnSuccessListener { snapshot ->
                val post  = snapshot.getValue(Post::class.java)
                if (post != null) {
                    currentPost = post
                }
                binding.loadingProgress.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false

                Glide.with(this)
                    .load(post!!.userProfileUrl)
                    .apply(RequestOptions().centerCrop())
                    .into(binding.profilePicture)

                if (post.contentUrl.isBlank()) {
                    binding.content.visibility = View.GONE
                } else {
                    binding.content.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(post.contentUrl)
                        .apply(RequestOptions().centerCrop())
                        .into(binding.content)
                }

                binding.time.text  = Utils.formatDate(post.time)
                binding.username.text  = post.username

                // Create a SpannableStringBuilder to hold the styled text
                val styledText = SpannableStringBuilder(post.text)

                // Define the pattern to find hashtags
                val hashTagPattern = Pattern.compile("#[a-zA-Z0-9]+")

                // Find hashtags in the text and apply styling
                val matcher: Matcher = hashTagPattern.matcher(post.text)
                while (matcher.find()) {
                    // Apply blue color to hashtags
                    styledText.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary)),
                        matcher.start(),
                        matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                // Set the styled text to the TextView
                binding.text.text = styledText

                binding.likes.text  = Utils.formatSocialMediaNumber(post.likes)
                binding.shares.text  = Utils.formatSocialMediaNumber(post.shares)
                binding.comments.text  = Utils.formatSocialMediaNumber(post.comments)
                binding.views.text  = Utils.formatSocialMediaNumber(post.views)

                if (userLikes.contains(post.id)) {
                    binding.likeIcon.setImageResource(R.drawable.full_heart_icon)
                    binding.likeIcon.setColorFilter(Color.RED)
                } else {
                    binding.likeIcon.setImageResource(R.drawable.empty_heart_icon)
                    binding.likeIcon.setColorFilter(Color.BLACK)
                }


                binding.share.setOnClickListener {}


                binding.comment.setOnClickListener {

                    if (user != null) {
                        val intent = Intent(this, CommentsActivity::class.java)
                        intent.putExtra("postId", post.id)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, SignInActivity::class.java)
                        startActivity(intent)


                    }
                }

                binding.like.setOnClickListener {

                        if (user != null) {
                            val reelsReference = database.getReference("users/${user.uid}/likes")
                            reelsReference.get().addOnSuccessListener { snapshot ->
                                if (snapshot.exists()) {
                                    val editor = getSharedPreferences("SportNet", 0).edit()
                                    val gson = GsonBuilder().create()
                                    val likes: ArrayList<String> = gson.fromJson(
                                        snapshot.value.toString(),
                                        Utils.arrayListOfStringsToken
                                    )
                                    val postId = post.id
                                    if (likes.contains(postId)) {
                                        binding.likeIcon.setImageResource(R.drawable.empty_heart_icon)
                                        likes.remove(postId)
                                        userLikes.remove(postId)
                                        post.likes--
                                        binding.likes.text = post.likes.toString()
                                        binding.likeIcon.setColorFilter(Color.BLACK)
                                        updateLikesInRealtimeDatabase(postId, false)
                                    } else {
                                        binding.likeIcon.setImageResource(R.drawable.full_heart_icon)
                                        likes.add(postId)
                                        userLikes.add(postId)
                                        post.likes++
                                        binding.likes.text = post.likes.toString()
                                        binding.likeIcon.setColorFilter(Color.RED)
                                        updateLikesInRealtimeDatabase(postId, true)
                                    }
                                    val likesJson = GsonBuilder().create().toJson(likes)
                                    updateUserLikes(user.uid, likes, likesJson)
                                    editor.putString("likes", likesJson)
                                    editor.apply()
                                } else {
                                    val editor = getSharedPreferences("SportNet", 0).edit()
                                    val likes = userLikes
                                    val postId = post.id
                                    if (likes.contains(postId)) {
                                        binding.likeIcon.setImageResource(R.drawable.empty_heart_icon)
                                        likes.remove(postId)
                                        userLikes.remove(postId)
                                        post.likes--
                                        binding.likes.text = post.likes.toString()
                                        binding.likeIcon.setColorFilter(Color.BLACK)
                                        updateLikesInRealtimeDatabase(postId, false)
                                    } else {
                                        binding.likeIcon.setImageResource(R.drawable.full_heart_icon)
                                        likes.add(postId)
                                        userLikes.add(postId)
                                        post.likes++
                                        binding.likes.text = post.likes.toString()
                                        binding.likeIcon.setColorFilter(Color.RED)
                                        updateLikesInRealtimeDatabase(postId, true)
                                    }
                                    val likesJson = GsonBuilder().create().toJson(likes)
                                    updateUserLikes(user.uid, likes, likesJson)
                                    editor.putString("likes", likesJson)
                                    editor.apply()
                                }
                            }.addOnFailureListener { exception -> }

                        } else {
                            val intent = Intent(this, SignInActivity::class.java)
                            startActivity(intent)
                        }
                }

                updateViewsInRealtimeDatabase(post.id, post)

            }
            .addOnFailureListener { exception ->
                binding.loadingProgress.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
            }
    }


    private fun updateViewsInRealtimeDatabase(postId: String, post: Post) {
        var tries = 0
        val reelsReference = database.getReference("posts/$postId/views")

        // Use setValue with ServerValue.increment to update the value
        reelsReference.setValue(ServerValue.increment(1))
            .addOnSuccessListener {
                // Handle success
                tries = 0
                post.views ++
                binding.views.text = post.views.toString()
            }
            .addOnFailureListener { e ->
                // Handle failure
                tries++
                if (tries < 3) {
                    updateViewsInRealtimeDatabase(postId, post)
                }
            }
    }


    private fun updateUserLikes(userId: String, likes: ArrayList<String>, userLikesString : String) {

        val reelsReference = database.getReference("users/$userId/likes")

        if (likes == userLikes) {
            val likesJson = GsonBuilder().create().toJson(likes)
            reelsReference.setValue(likesJson)
                .addOnSuccessListener {
                    // Handle success
                    Log.i("allFirebaseChanges","updating likes success")
                }
                .addOnFailureListener { e ->
                    // Handle the failure, e.g., connection issues or permission denied
                    Log.i("allFirebaseChanges","updating likes error: ${e.message}")
                }
        } else {
            reelsReference.setValue(userLikesString)
                .addOnSuccessListener {
                    // Handle success
                    Log.i("allFirebaseChanges","updating likes success")
                }
                .addOnFailureListener { e ->
                    // Handle the failure, e.g., connection issues or permission denied
                    Log.i("allFirebaseChanges","updating likes error: ${e.message}")
                }
        }
    }


    private fun updateLikesInRealtimeDatabase(postId: String, like: Boolean) {

        val reelsReference = database.getReference("posts/$postId/likes")

        if (like) {
            reelsReference.setValue(ServerValue.increment(1))
                .addOnSuccessListener {
                    // Handle success
                }
                .addOnFailureListener { e ->
                    // Handle failure
                }
        } else {
            reelsReference.setValue(ServerValue.increment(-1))
                .addOnSuccessListener {
                    // Handle success
                }
                .addOnFailureListener { e ->
                    // Handle failure
                }
        }
    }

}