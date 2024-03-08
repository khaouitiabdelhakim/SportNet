package com.ensias.sportnet.fragments



import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ensias.sportnet.MainActivity
import com.ensias.sportnet.R
import com.ensias.sportnet.authentication.SignInActivity
import com.ensias.sportnet.databinding.FragmentProfileBinding
import com.ensias.sportnet.showers.ProfileActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        binding = FragmentProfileBinding.bind(view)

        auth = FirebaseAuth.getInstance()

        if(auth.currentUser != null ) {
            binding.email.text = auth.currentUser!!.email
            binding.authenticationType.text = "Sign Out"
            Glide.with(this)
                .load(MainActivity.currentUser.profilePictureUrl)
                .apply(RequestOptions().centerCrop())
                .into(binding.profile)
        } else {
            binding.email.text = "sign in to get more features"
            binding.authenticationType.text = "Sign IN"
        }

        binding.signOut.setOnClickListener {
            if(auth.currentUser == null ) {
                startActivity(Intent(requireActivity(),SignInActivity::class.java))
            } else {
                binding.signOut.isClickable = false
                binding.signOutProgress.visibility = View.VISIBLE
                binding.signOutText.visibility = View.INVISIBLE
                signOut()
            }

        }

        binding.profile.setOnClickListener {
            if(auth.currentUser != null ) {
                val intent = Intent(context, ProfileActivity::class.java)
                intent.putExtra("userId",MainActivity.currentUser.id)
                ContextCompat.startActivity(requireContext(), intent, null)
            }else {
                startActivity(Intent(requireActivity(), SignInActivity::class.java))
            }

        }

        return view
    }

    private fun signOut() {
        auth.signOut()
        // Sign-out success
        val intent = Intent(requireActivity(), SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

}
