package com.abdelhakim.sportnet.editors


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdelhakim.sportnet.MainActivity
import com.abdelhakim.sportnet.adapters.CommentAdapter
import com.abdelhakim.sportnet.databinding.ActivityCommentsBinding
import com.abdelhakim.sportnet.fragments.HomeFragment
import com.abdelhakim.sportnet.models.Comment
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

        binding.loadingProgress.visibility = View.VISIBLE

        binding.commentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CommentsActivity)
        }

        postComments.clear()

        adapter = CommentAdapter(this@CommentsActivity, postComments)

        binding.commentsRecyclerView.adapter  = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            // Handle the refresh logic, for example, reload the comments
            loadComments()
        }

    }

    override fun onResume() {
        super.onResume()

        loadComments()
    }
    private fun loadComments() {
        val postId = intent.getStringExtra("postId")
        val position = intent.getIntExtra("position",-1)
        val user = auth.currentUser

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

                binding.loadingProgress.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false

                Log.d("CommentsLoading", "Comments data loaded ${comments.size}")
            }
            .addOnFailureListener { exception ->
                binding.loadingProgress.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                Log.e("FirebaseError", "Categories retrieval error: $exception")
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