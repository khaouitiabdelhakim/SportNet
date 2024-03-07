package com.abdelhakim.sportnet.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.abdelhakim.sportnet.MainActivity.Companion.userLikes
import com.abdelhakim.sportnet.R
import com.abdelhakim.sportnet.authentication.SignInActivity
import com.abdelhakim.sportnet.databinding.PostViewBinding
import com.abdelhakim.sportnet.editors.CommentsActivity
import com.abdelhakim.sportnet.models.Post
import com.abdelhakim.sportnet.showers.SinglePostActivity
import com.abdelhakim.sportnet.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.gson.GsonBuilder


class PostAdapter(private val context: Context, private var posts: ArrayList<Post>): RecyclerView.Adapter<PostAdapter.MyHolder>(){



    // for loading more posts
    private val postsPerPage = 15

    private val loadThreshold = 5



    // firebase
    private val authentication = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    var startAt: String? = null

    companion object {
        var startFetchingNewPosts = false
        var currentPage = 0

    }


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
        val profile = binding.profilePicture
        val time = binding.time

        val root = binding.root
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {

        return MyHolder(PostViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    private val holders = mutableMapOf<Int, MyHolder>()

    @SuppressLint("CommitPrefEdits", "ResourceAsColor")
    override fun onBindViewHolder(holder: MyHolder, @SuppressLint("RecyclerView") position: Int) {



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
        holder.text.text  = posts[position].text
        holder.text.text  = posts[position].text

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


        holder.username.setOnClickListener {


        }

        if (position >= posts.size - loadThreshold && !startFetchingNewPosts) {
            startFetchingNewPosts = true
            loadMorePosts()
        }


        // Store the holder for later access in play/pause methods
        holders[position] = holder


        holder.share.setOnClickListener {}


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
                    if (snapshot.exists()) {
                        val editor = context.getSharedPreferences("SportNet", 0).edit()
                        val gson = GsonBuilder().create()
                        val likes: ArrayList<String> = gson.fromJson(snapshot.value.toString(), Utils.arrayListOfStringsToken)
                        val postId = posts[position].id
                        if (likes.contains(postId)) {
                            holder.likeIcon.setImageResource(R.drawable.empty_heart_icon)
                            likes.remove(postId)
                            userLikes.remove(postId)
                            posts[position].likes--
                            holder.likes.text  = posts[position].likes.toString()
                            holder.likeIcon.setColorFilter(Color.BLACK)
                            updateLikesInRealtimeDatabase(postId, false)
                        } else {
                            holder.likeIcon.setImageResource(R.drawable.full_heart_icon)
                            likes.add(postId)
                            userLikes.add(postId)
                            posts[position].likes++
                            holder.likes.text  = posts[position].likes.toString()
                            holder.likeIcon.setColorFilter(Color.RED)
                            updateLikesInRealtimeDatabase(postId, true)
                        }
                        val likesJson = GsonBuilder().create().toJson(likes)
                        updateUserLikes(user.uid,likes, likesJson)
                        editor.putString("likes", likesJson)
                        editor.apply()
                    } else {
                        val editor = context.getSharedPreferences("SportNet", 0).edit()
                        val likes = userLikes
                        val postId = posts[position].id
                        if (likes.contains(postId)) {
                            holder.likeIcon.setImageResource(R.drawable.empty_heart_icon)
                            likes.remove(postId)
                            userLikes.remove(postId)
                            posts[position].likes--
                            holder.likes.text  = posts[position].likes.toString()
                            holder.likeIcon.setColorFilter(Color.BLACK)
                            updateLikesInRealtimeDatabase(postId, false)
                        } else {
                            holder.likeIcon.setImageResource(R.drawable.full_heart_icon)
                            likes.add(postId)
                            userLikes.add(postId)
                            posts[position].likes++
                            holder.likes.text  = posts[position].likes.toString()
                            holder.likeIcon.setColorFilter(Color.RED)
                            updateLikesInRealtimeDatabase(postId, true)
                        }
                        val likesJson = GsonBuilder().create().toJson(likes)
                        updateUserLikes(user.uid,likes, likesJson)
                        editor.putString("likes", likesJson)
                        editor.apply()
                    }
                }.addOnFailureListener { exception ->
                }
            }
            else {
                val intent = Intent(context, SignInActivity::class.java)
                ContextCompat.startActivity(context, intent, null)
            }

        }

        updateViewsInRealtimeDatabase(posts[position].id, position)


    }

    fun loadMorePosts() {

        val reelsReference = database.getReference("posts")

        val query = reelsReference
            .orderByChild("id")
            .startAfter(startAt)
            .limitToFirst(postsPerPage)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val newPosts = ArrayList<Post>()
                for (childSnapshot in snapshot.children) {
                    val post = childSnapshot.getValue(Post::class.java)
                    if (post != null && !posts.contains(post)) {
                        newPosts.add(post)
                        startAt = post.id
                    }
                }


                // Shuffle the new posts to randomize the order
                newPosts.shuffle()

                // Add the new posts to the existing ArrayList
                posts.addAll(newPosts)

                // Notify the adapter about the inserted items
                notifyDataSetChanged()
                currentPage++
                startFetchingNewPosts =  newPosts.isEmpty()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("allFirebaseChanges", "Error loading more Posts retrieval error: $databaseError")
                startFetchingNewPosts = false
            }
        })
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

    fun getViewHolder(positionToCheck: Int): MyHolder? {
        for (i in 0 until holders.size) {
            return holders[i]
        }
        return null
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