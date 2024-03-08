package com.ensias.sportnet.fragments



import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ensias.sportnet.MainActivity
import com.ensias.sportnet.R
import com.ensias.sportnet.adapters.AccountAdapter
import com.ensias.sportnet.databinding.FragmentAccountsBinding
import com.ensias.sportnet.models.User
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AccountsFragment : Fragment() {

    private lateinit var binding: FragmentAccountsBinding
    private lateinit var auth: FirebaseAuth
    private var accounts = ArrayList<User>()

    @SuppressLint("StaticFieldLeak")
    companion object {
        lateinit var adapter: AccountAdapter
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_accounts, container, false)
        binding = FragmentAccountsBinding.bind(view)

        auth = FirebaseAuth.getInstance()

        if(auth.currentUser != null ) {
            binding.username.text = MainActivity.currentUser.username
            binding.time.text = "My Account"
            Glide.with(this)
                .load(MainActivity.currentUser.profilePictureUrl)
                .apply(RequestOptions().centerCrop())
                .into(binding.profilePicture)
        } else {
            binding.username.text = "Sign In For More Features"
            binding.time.text = "My Account"
            Glide.with(this)
                .load(R.drawable.rounded_profile)
                .apply(RequestOptions().centerCrop())
                .into(binding.profilePicture)
        }
        binding.result.visibility = View.GONE


        binding.searchView.apply {
            setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                @SuppressLint("SetTextI18n")
                override fun onQueryTextSubmit(query: String?): Boolean {
                    binding.searchView.clearFocus()
                    binding.loadingProgress.visibility = View.VISIBLE
                    binding.result.visibility = View.GONE

                    // Check if the query is not null or empty
                    query?.let {
                        // Query the users with usernames containing the query
                        val queryRef = FirebaseDatabase.getInstance().getReference("users")
                            .orderByChild("username").startAt(query).endAt(query + "\uf8ff")

                        queryRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // Clear the existing accounts list
                                accounts.clear()

                                // Iterate through the snapshot to get users matching the query
                                for (userSnapshot in snapshot.children) {
                                    val user = userSnapshot.getValue(User::class.java)
                                    if (user != null) {
                                        accounts.add(user)
                                    }
                                }

                                // Update the adapter
                                adapter.notifyDataSetChanged()

                                // Hide loading progress
                                binding.loadingProgress.visibility = View.GONE

                                // Show a message if no accounts found
                                if (accounts.isEmpty()) {
                                    // Show a message indicating no accounts found
                                    binding.result.visibility = View.VISIBLE

                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                                binding.loadingProgress.visibility = View.GONE
                                binding.result.visibility = View.GONE
                                // Show a message indicating query cancellation or error
                            }
                        })
                    }

                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return true
                }
            })
        }


        binding.accountsRecyclerView.layoutManager = LinearLayoutManager(requireContext(),  LinearLayoutManager.VERTICAL, false)
        adapter = AccountAdapter(requireContext(),accounts)
        binding.accountsRecyclerView.adapter = adapter

        return view

    }


}
