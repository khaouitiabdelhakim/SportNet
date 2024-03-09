package com.ensias.sportnet.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ensias.sportnet.MainActivity.Companion.userLikes
import com.ensias.sportnet.R
import com.ensias.sportnet.authentication.SignInActivity
import com.ensias.sportnet.databinding.PostViewBinding
import com.ensias.sportnet.editors.CommentsActivity
import com.ensias.sportnet.models.Post
import com.ensias.sportnet.showers.AccountShowerActivity
import com.ensias.sportnet.showers.SinglePostActivity
import com.ensias.sportnet.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ensias.sportnet.MainActivity
import com.ensias.sportnet.editors.EditPostActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.gson.GsonBuilder
import java.util.regex.Matcher
import java.util.regex.Pattern


class OwnPostAdapter(private val context: Context, private var posts: ArrayList<Post>): RecyclerView.Adapter<OwnPostAdapter.MyHolder>(){


    // firebase
    private val authentication = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()



    class MyHolder(binding: PostViewBinding): RecyclerView.ViewHolder(binding.root){

        val text = binding.text
        val likes = binding.likes
        val shares = binding.shares
        val share = binding.share
        val content = binding.content
        val like = binding.like
        val likeIcon = binding.likeIcon
        val comments = binding.comments
        val comment = binding.comment
        val views = binding.views

        val username = binding.username
        val profileSection = binding.profileSection
        val profile = binding.profilePicture
        val time = binding.time

        val edit = binding.edit

        val root = binding.root
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {

        return MyHolder(PostViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    private val holders = mutableMapOf<Int, MyHolder>()

    @SuppressLint("CommitPrefEdits", "ResourceAsColor")
    override fun onBindViewHolder(holder: MyHolder, @SuppressLint("RecyclerView") position: Int) {


        if (authentication.currentUser != null && MainActivity.currentUser.id == posts[position].userId) {
            holder.edit.visibility = View.VISIBLE
            holder.edit.setOnClickListener {
                val intent = Intent(context, EditPostActivity::class.java)
                intent.putExtra("postId",posts[position].id)
                ContextCompat.startActivity(context, intent, null)
            }
        } else {
            holder.edit.visibility = View.INVISIBLE
        }

        holder.root.setOnClickListener {
            val intent = Intent(context, SinglePostActivity::class.java)
            intent.putExtra("postId",posts[position].id)
            ContextCompat.startActivity(context, intent, null)
        }

        Glide.with(context)
            .load(posts[position].userProfileUrl)
            .apply(RequestOptions().centerCrop())
            .into(holder.profile)

        if (posts[position].contentUrl.isBlank()) {
            holder.content.visibility = View.GONE
        } else {
            holder.content.visibility = View.VISIBLE
            Glide.with(context)
                .load(posts[position].contentUrl)
                .apply(RequestOptions().centerCrop())
                .into(holder.content)
        }

        holder.time.text  = Utils.formatDate(posts[position].time)
        holder.username.text  = posts[position].username

        // Create a SpannableStringBuilder to hold the styled text
        val styledText = SpannableStringBuilder(posts[position].text)

        // Define the pattern to find hashtags
        val hashTagPattern = Pattern.compile("#[a-zA-Z0-9]+")

        // Find hashtags in the text and apply styling
        val matcher: Matcher = hashTagPattern.matcher(posts[position].text)
        while (matcher.find()) {
            // Apply blue color to hashtags
            styledText.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.primary)),
                matcher.start(),
                matcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Set the styled text to the TextView
        holder.text.text = styledText

        holder.likes.text  = Utils.formatSocialMediaNumber(posts[position].likes)
        holder.shares.text  = Utils.formatSocialMediaNumber(posts[position].shares)
        holder.comments.text  = Utils.formatSocialMediaNumber(posts[position].comments)
        holder.views.text  = Utils.formatSocialMediaNumber(posts[position].views)

        if (userLikes.contains(posts[position].id)) {
            holder.likeIcon.setImageResource(R.drawable.full_heart_icon)
            holder.likeIcon.setColorFilter(Color.RED)
        } else {
            holder.likeIcon.setImageResource(R.drawable.empty_heart_icon)
            holder.likeIcon.setColorFilter(Color.BLACK)
        }


        // Store the holder for later access in play/pause methods
        holders[position] = holder


        holder.share.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND

            // Set the text to share including the post text and content URL
            val textToShare = "${posts[position].text}\n\n${posts[position].contentUrl}"

            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare)

            // Start the activity to share
            context.startActivity(Intent.createChooser(shareIntent, "Share via")
                .apply {
                    // Check if sharing was successful
                    val shareSuccessful = true // Set this to true if sharing was successful, false otherwise
                    if (shareSuccessful) {
                        // Update shares count in Firebase
                        val postId = posts[position].id
                        updateSharesInRealtimeDatabase(postId)

                        // Update the UI to reflect the changes
                        holder.shares.text = Utils.formatSocialMediaNumber(posts[position].shares)
                    }
                }
            )
        }

        holder.profileSection.setOnClickListener {
            val intent = Intent(context, AccountShowerActivity::class.java)
            intent.putExtra("accountId",posts[position].userId)
            ContextCompat.startActivity(context, intent, null)
        }




        holder.comment.setOnClickListener {
            val user = authentication.currentUser
            if (user != null) {
                val intent = Intent(context, CommentsActivity::class.java)
                intent.putExtra("postId",posts[position].id)
                intent.putExtra("position",position)
                ContextCompat.startActivity(context, intent, null)
            }
            else {
                val intent = Intent(context, SignInActivity::class.java)
                ContextCompat.startActivity(context, intent, null)
            }


        }

        holder.like.setOnClickListener {

            val user = authentication.currentUser
            if (user != null) {
                val reelsReference = database.getReference("users/${user.uid}/likes")
                reelsReference.get().addOnSuccessListener { snapshot ->
                    val gson = GsonBuilder().create()
                    val likes: ArrayList<String> = gson.fromJson(snapshot.value.toString(), Utils.arrayListOfStringsToken)
                    val postId = posts[position].id
                    val isLiked = likes.contains(postId)

                    if (isLiked) {
                        likes.remove(postId)
                        userLikes.remove(postId)
                        posts[position].likes--
                        holder.likeIcon.setImageResource(R.drawable.empty_heart_icon)
                        holder.likeIcon.setColorFilter(Color.BLACK)
                    } else {
                        likes.add(postId)
                        userLikes.add(postId)
                        posts[position].likes++
                        holder.likeIcon.setImageResource(R.drawable.full_heart_icon)
                        holder.likeIcon.setColorFilter(Color.RED)
                    }

                    holder.likes.text = posts[position].likes.toString()
                    updateLikesInRealtimeDatabase(postId, !isLiked)
                    val likesJson = GsonBuilder().create().toJson(likes)
                    updateUserLikes(user.uid, likes, likesJson)
                }.addOnFailureListener { exception ->
                    Log.e("LikeAction", "Failed to fetch likes: ${exception.message}")
                }
            } else {
                val intent = Intent(context, SignInActivity::class.java)
                ContextCompat.startActivity(context, intent, null)
            }
        }

        updateViewsInRealtimeDatabase(posts[position].id, position)


    }

    private fun updateSharesInRealtimeDatabase(postId: String) {
        val reelsReference = database.getReference("posts/$postId/shares")

        // Increment shares count by 1
        reelsReference.setValue(ServerValue.increment(1))
            .addOnSuccessListener {
                // Handle success
                Log.i("UpdateShares", "Shares count updated successfully")
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.e("UpdateShares", "Failed to update shares count: ${e.message}")
            }
    }


    override fun getItemCount(): Int {
        return posts.size
    }




    fun updatePostComments(position: Int) {
        if (position >= 0 && position < posts.size) {
            posts[position].comments++
            notifyItemChanged(position)
        }
    }



    private fun updateViewsInRealtimeDatabase(postId: String, position: Int) {
        var tries = 0
        val reelsReference = database.getReference("posts/$postId/views")

        // Use setValue with ServerValue.increment to update the value
        reelsReference.setValue(ServerValue.increment(1))
            .addOnSuccessListener {
                // Handle success
                tries = 0
                if (position >= 0 && position < posts.size) {
                    val holder = holders[position]
                    posts[position].views ++
                    val viewsTextView = holder!!.views
                    viewsTextView.text = posts[position].views.toString()
                }
            }
            .addOnFailureListener { e ->
                // Handle failure
                tries++
                if (tries < 3) {
                    updateViewsInRealtimeDatabase(postId, position)
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