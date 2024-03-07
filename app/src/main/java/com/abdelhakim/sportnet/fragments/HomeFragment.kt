package com.abdelhakim.sportnet.fragments



import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdelhakim.sportnet.MainActivity
import com.abdelhakim.sportnet.R
import com.abdelhakim.sportnet.adapters.PostAdapter
import com.abdelhakim.sportnet.authentication.SignInActivity
import com.abdelhakim.sportnet.databinding.FragmentHomeBinding
import com.abdelhakim.sportnet.editors.PostActivity
import com.abdelhakim.sportnet.models.Post
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth

    val posts = ArrayList<Post>()

    @SuppressLint("StaticFieldLeak")
    companion object {
        lateinit var adapter: PostAdapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.bind(view)
        auth = FirebaseAuth.getInstance()

        if(auth.currentUser != null ) {
            Glide.with(this)
                .load(MainActivity.currentUser.profilePictureUrl)
                .apply(RequestOptions().centerCrop())
                .into(binding.profile)
        }


        // posting text
        binding.startAPost.setOnClickListener {
            if(auth.currentUser != null ) {
                val intent = Intent(requireActivity(), PostActivity::class.java)
                intent.putExtra("contentType","article")
                startActivity(intent)
            }else {
                startActivity(Intent(requireActivity(), SignInActivity::class.java))
            }
        }

        // posting video
        binding.video.setOnClickListener {
            if(auth.currentUser != null ) {
                val intent = Intent(requireActivity(), PostActivity::class.java)
                intent.putExtra("contentType","video")
                startActivity(intent)
            }else {
                startActivity(Intent(requireActivity(), SignInActivity::class.java))
            }
        }

        // posting picture
        binding.picture.setOnClickListener {
            if(auth.currentUser != null ) {
                val intent = Intent(requireActivity(), PostActivity::class.java)
                intent.putExtra("contentType","image")
                startActivity(intent)
            }else {
                startActivity(Intent(requireActivity(), SignInActivity::class.java))
            }
        }

        // posting article
        binding.article.setOnClickListener {
            if(auth.currentUser != null ) {
                val intent = Intent(requireActivity(), PostActivity::class.java)
                intent.putExtra("contentType","article")
                startActivity(intent)
            }else {
                startActivity(Intent(requireActivity(), SignInActivity::class.java))
            }
        }

        binding.postsRecyclerView.layoutManager = LinearLayoutManager(requireContext(),  LinearLayoutManager.VERTICAL, false)
        adapter = PostAdapter(requireContext(),posts)
        binding.postsRecyclerView.adapter = adapter
        adapter.loadMorePosts()

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
        }

        return view

    }




}
