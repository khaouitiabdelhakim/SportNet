package com.ensias.sportnet.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.ensias.sportnet.databinding.AccountViewBinding
import com.ensias.sportnet.models.User
import com.ensias.sportnet.showers.AccountShowerActivity
import com.ensias.sportnet.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class AccountAdapter(private val context: Context, private val accounts: List<User>) : RecyclerView.Adapter<AccountAdapter.MyHolder>() {

    class MyHolder(private val binding: AccountViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            with(binding) {
                Glide.with(root)
                    .load(user.profilePictureUrl)
                    .apply(RequestOptions().centerCrop())
                    .into(profilePicture)

                username.text = user.username
                since.text = Utils.formatDate(user.createdAt)

                root.setOnClickListener {
                    val intent = Intent(root.context, AccountShowerActivity::class.java)
                    intent.putExtra("accountId", user.id)
                    startActivity(root.context, intent, null)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = AccountViewBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyHolder(binding)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.bind(accounts[position])
    }

    override fun getItemCount(): Int = accounts.size
}
