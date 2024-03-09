package com.ensias.sportnet.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ensias.sportnet.MainActivity
import com.ensias.sportnet.MainActivity.Companion.userCommentsLikes
import com.ensias.sportnet.R
import com.ensias.sportnet.authentication.SignInActivity
import com.ensias.sportnet.databinding.CommentViewBinding
import com.ensias.sportnet.models.Comment
import com.ensias.sportnet.showers.AccountShowerActivity
import com.ensias.sportnet.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.gson.GsonBuilder


class CommentAdapter (private val context: Context, private var comments: ArrayList<Comment>): RecyclerView.Adapter<CommentAdapter.MyHolder>(){


    // firebase
    private val authentication = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    class MyHolder(binding: CommentViewBinding): RecyclerView.ViewHolder(binding.root){

        val comment = binding.comment
        val username = binding.username
        val profilePicture = binding.profilePicture
        val likes = binding.likes
        val likeIcon = binding.likeIcon
        val date = binding.time
        val root = binding.root

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {

        return MyHolder(CommentViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    @SuppressLint("CheckResult")

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        try {

            Glide.with(context)
                .load(comments[position].profilePictureUrl)
                .apply(RequestOptions().centerCrop())
                .into(holder.profilePicture)

            holder.username.text = comments[position].username
            holder.date.text = Utils.formatDate(comments[position].createdAt)
            holder.comment.text = comments[position].comment
            holder.likes.text =  comments[position].likes.toString()


            holder.username.setOnClickListener {
                val intent = Intent(context, AccountShowerActivity::class.java)
                intent.putExtra("accountId",comments[position].userId)
                ContextCompat.startActivity(context, intent, null)
            }

            if (userCommentsLikes.contains(comments[position].id)) {
                holder.likeIcon.setImageResource(R.drawable.full_heart_icon)
                holder.likeIcon.setColorFilter(Color.RED)
            } else {
                holder.likeIcon.setImageResource(R.drawable.empty_heart_icon)
                holder.likeIcon.setColorFilter(Color.BLACK)
            }


            holder.likeIcon.setOnClickListener {
                val user = authentication.currentUser
                holder.likeIcon.setColorFilter(Color.RED)

                if (user != null) {
                    val reelsReference = database.getReference("users/${user.uid}/commentsLikes")
                    reelsReference.get().addOnSuccessListener { snapshot ->
                        val gson = GsonBuilder().create()
                        val likes: ArrayList<String> = gson.fromJson(snapshot.value.toString(), Utils.arrayListOfStringsToken)
                        val commentId = comments[position].id
                        val isLiked = likes.contains(commentId)

                        if (isLiked) {
                            likes.remove(commentId)
                            userCommentsLikes.remove(commentId)
                            comments[position].likes--
                            holder.likeIcon.setImageResource(R.drawable.empty_heart_icon)
                            holder.likeIcon.setColorFilter(Color.BLACK)
                        } else {
                            likes.add(commentId)
                            userCommentsLikes.add(commentId)
                            comments[position].likes++
                            holder.likeIcon.setImageResource(R.drawable.full_heart_icon)
                            holder.likeIcon.setColorFilter(Color.RED)
                        }

                        holder.likes.text = comments[position].likes.toString()
                        updateLikesInRealtimeDatabase(commentId, !isLiked)
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


        } catch (_: Exception) {}


    }

    override fun getItemCount(): Int {
        return comments.size
    }

    private fun updateUserLikes(userId: String, likes: ArrayList<String>, userCommentsLikesString : String) {

        val reelsReference = database.getReference("users/$userId/commentsLikes")

        if (likes == MainActivity.userLikes) {
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
            reelsReference.setValue(userCommentsLikesString)
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


    private fun updateLikesInRealtimeDatabase(commentId: String, like: Boolean) {

        val reelsReference = database.getReference("comments/$commentId/likes")

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