package com.ensias.sportnet.editors


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.ensias.sportnet.MainActivity
import com.ensias.sportnet.adapters.CommentAdapter
import com.ensias.sportnet.databinding.ActivityCommentsBinding
import com.ensias.sportnet.fragments.HomeFragment
import com.ensias.sportnet.models.Comment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class CommentsActivity : AppCompatActivity() {

    lateinit var binding: ActivityCommentsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var adapter: CommentAdapter

    companion object {
        val postComments = ArrayList<Comment>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        if(auth.currentUser != null ) {
            binding.username.text = MainActivity.currentUser.username
            Glide.with(this)
                .load(MainActivity.currentUser.profilePictureUrl)
                .apply(RequestOptions().centerCrop())
                .into(binding.profile)
        }



        binding.commentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CommentsActivity)
        }

        postComments.clear()

        adapter = CommentAdapter(this@CommentsActivity, postComments)

        binding.commentsRecyclerView.adapter  = adapter



    }

    override fun onResume() {
        super.onResume()

        loadComments()
    }
    private fun loadComments() {
        val postId = intent.getStringExtra("postId")
        val position = intent.getIntExtra("position",-1)
        val user = auth.currentUser
        binding.result.visibility = View.GONE
        binding.loading.visibility = View.VISIBLE

        val categoryReference = database.getReference("comments/$postId")

        categoryReference.get()
            .addOnSuccessListener { snapshot ->
                val comments = mutableListOf<Comment>()

                for (childSnapshot in snapshot.children.reversed()) {
                    val comment = childSnapshot.getValue(Comment::class.java)
                    if (comment != null) {
                        comments.add(comment)
                    }
                }

                postComments.clear()
                postComments.addAll(comments)

                adapter = CommentAdapter(this@CommentsActivity, postComments)

                binding.commentsRecyclerView.adapter  = adapter


                binding.loading.visibility = View.GONE

                if(comments.isEmpty())  binding.result.visibility = View.VISIBLE

                Log.d("CommentsLoading", "Comments data loaded ${comments.size}")
            }
            .addOnFailureListener { exception ->
                binding.loading.visibility = View.GONE
                Log.e("FirebaseError", "Categories retrieval error: $exception")
                binding.result.visibility = View.VISIBLE
            }


        binding.sendComment.setOnClickListener {
            val commentText = binding.comment.text.toString().trim()

            if (commentText.isEmpty()) {
                return@setOnClickListener
            }

            binding.sendComment.isClickable = false
            binding.sendCommentText.visibility = View.INVISIBLE
            binding.sendCommentProgress.visibility = View.VISIBLE

            val commentsReference = database.getReference("comments/$postId")

            // generate a unique id for the comment
            val commentId = commentsReference.push().key

            val comment = Comment(
                id = commentId!!,
                comment = commentText,
                userId = user!!.uid,
                username = MainActivity.currentUser.username,
                profilePictureUrl = MainActivity.currentUser.profilePictureUrl,
                postId = postId.toString(),
                createdAt = System.currentTimeMillis(), // default value for createdAt
                likes = 0

            )

            // set the comment with the generated id
            commentsReference.child(commentId).setValue(comment)
                .addOnSuccessListener {

                    binding.sendCommentText.visibility = View.VISIBLE
                    binding.sendCommentProgress.visibility = View.INVISIBLE
                    binding.sendComment.isClickable = true

                    postComments.add(0,comment)
                    adapter.notifyItemInserted(0)

                    if (postId != null) {
                        updateCommentsInRealtimeDatabase(postId)
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the failure, e.g., connection issues or permission denied

                    binding.sendCommentText.visibility = View.VISIBLE
                    binding.sendCommentProgress.visibility = View.INVISIBLE
                    binding.sendComment.isClickable = true

                    HomeFragment.adapter.updatePostComments(position)

                    exception.message?.let { it1 -> Log.e("errocomment", it1) }
                }
        }
    }

    private fun updateCommentsInRealtimeDatabase(postId: String) {
        val database = FirebaseDatabase.getInstance()
        val reelsReference = database.getReference("posts/$postId/comments")

        // Read the current views count
        reelsReference.setValue(ServerValue.increment(1))
            .addOnSuccessListener {
                // Handle success
                binding.comment.setText("")
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }
}