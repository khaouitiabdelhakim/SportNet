package com.abdelhakim.sportnet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.abdelhakim.sportnet.databinding.ActivityMainBinding
import com.abdelhakim.sportnet.fragments.HomeFragment
import com.abdelhakim.sportnet.fragments.ProfileFragment
import com.abdelhakim.sportnet.fragments.RoomsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    // for bottom nav bar
    lateinit var bottomNav : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        loadFragment(HomeFragment())

        bottomNav = findViewById(R.id.bottomNav)

        bottomNav.menu.findItem(R.id.home).setIcon(R.drawable.home_full_icon)
        bottomNav.menu.findItem(R.id.rooms).setIcon(R.drawable.categories_empty_icon)
        bottomNav.menu.findItem(R.id.profile).setIcon(R.drawable.user_empty_icon)


        bottomNav.setOnItemSelectedListener { menuItem ->
            Log.d("clickBottomNavigation","click")
            when (menuItem.itemId) {
                R.id.home -> {
                    loadFragment(HomeFragment())
                    binding.bottomNav.menu.findItem(R.id.home).setIcon(R.drawable.home_full_icon)
                    binding.bottomNav.menu.findItem(R.id.rooms).setIcon(R.drawable.categories_empty_icon)
                    binding.bottomNav.menu.findItem(R.id.profile).setIcon(R.drawable.user_empty_icon)
                    true
                }
                R.id.rooms -> {
                    loadFragment(RoomsFragment())
                    binding.bottomNav.menu.findItem(R.id.home).setIcon(R.drawable.home_empty_icon)
                    binding.bottomNav.menu.findItem(R.id.rooms).setIcon(R.drawable.categories_full_icon)
                    binding.bottomNav.menu.findItem(R.id.profile).setIcon(R.drawable.user_empty_icon)
                    true
                }
                R.id.profile -> {
                    loadFragment(ProfileFragment())
                    binding.bottomNav.menu.findItem(R.id.home).setIcon(R.drawable.home_empty_icon)
                    binding.bottomNav.menu.findItem(R.id.rooms).setIcon(R.drawable.categories_empty_icon)
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