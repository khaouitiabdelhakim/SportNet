package com.ensias.sportnet.fragments



import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ensias.sportnet.MainActivity
import com.ensias.sportnet.R
import com.ensias.sportnet.adapters.PostAdapter
import com.ensias.sportnet.authentication.SignInActivity
import com.ensias.sportnet.databinding.FragmentHomeBinding
import com.ensias.sportnet.editors.PostActivity
import com.ensias.sportnet.models.Post
import com.ensias.sportnet.showers.AccountShowerActivity
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

        binding.profile.setOnClickListener {
            if(auth.currentUser != null ) {
                val intent = Intent(context, AccountShowerActivity::class.java)
                intent.putExtra("accountId",MainActivity.currentUser.id)
                ContextCompat.startActivity(requireContext(), intent, null)
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
