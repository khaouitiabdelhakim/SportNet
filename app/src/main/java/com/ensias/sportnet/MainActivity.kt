package com.ensias.sportnet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.ensias.sportnet.databinding.ActivityMainBinding
import com.ensias.sportnet.fragments.HomeFragment
import com.ensias.sportnet.fragments.ProfileFragment
import com.ensias.sportnet.fragments.AccountsFragment
import com.ensias.sportnet.models.User
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    // for bottom nav bar
    private lateinit var bottomNav : BottomNavigationView


    companion object {
        lateinit var currentUser: User
        var userLikes = ArrayList<String>()
        var userCommentsLikes = ArrayList<String>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTheme(R.style.Theme_SportNet)

        loadFragment(HomeFragment())

        bottomNav = findViewById(R.id.bottomNav)

        bottomNav.menu.findItem(R.id.home).setIcon(R.drawable.home_full_icon)
        bottomNav.menu.findItem(R.id.accounts).setIcon(R.drawable.categories_empty_icon)
        bottomNav.menu.findItem(R.id.profile).setIcon(R.drawable.user_empty_icon)


        bottomNav.setOnItemSelectedListener { menuItem ->
           
                R.id.accounts -> {
                    loadFragment(AccountsFragment())
                    binding.bottomNav.menu.findItem(R.id.home).setIcon(R.drawable.home_empty_icon)
                    binding.bottomNav.menu.findItem(R.id.accounts).setIcon(R.drawable.categories_full_icon)
                    binding.bottomNav.menu.findItem(R.id.profile).setIcon(R.drawable.user_empty_icon)
                    true
                }
                R.id.profile -> {
                    loadFragment(ProfileFragment())
                    binding.bottomNav.menu.findItem(R.id.home).setIcon(R.drawable.home_empty_icon)
                    binding.bottomNav.menu.findItem(R.id.accounts).setIcon(R.drawable.categories_empty_icon)
                    binding.bottomNav.menu.findItem(R.id.profile).setIcon(R.drawable.user_full_icon)
                    true
                }
                else -> false // Return false for unhandled items
            }
        }



    }



    private  fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container,fragment)
        transaction.commit()
    }



}