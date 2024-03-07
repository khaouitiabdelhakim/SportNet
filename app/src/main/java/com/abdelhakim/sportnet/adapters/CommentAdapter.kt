package com.abdelhakim.sportnet.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.abdelhakim.sportnet.databinding.CommentViewBinding
import com.abdelhakim.sportnet.models.Comment
import com.abdelhakim.sportnet.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


class CommentAdapter (private val context: Context, private var comments: ArrayList<Comment>): RecyclerView.Adapter<CommentAdapter.MyHolder>(){
    class MyHolder(binding: CommentViewBinding): RecyclerView.ViewHolder(binding.root){

        val comment = binding.comment
        val username = binding.username
        val profilePicture = binding.profilePicture
        val like = binding.like
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

            holder.like.setOnClickListener {

            }

        } catch (_: Exception) {}


    }

    override fun getItemCount(): Int {
        return comments.size
    }

}