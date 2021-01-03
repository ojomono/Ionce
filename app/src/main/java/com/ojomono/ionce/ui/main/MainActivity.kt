package com.ojomono.ionce.ui.main

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.auth.api.credentials.Credentials
import com.ojomono.ionce.R
import com.ojomono.ionce.firebase.Authentication

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup view pager (to allow swiping between tabs)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        val pageAdapter = MainFragmentPagerAdapter(supportFragmentManager)
        viewPager.adapter = pageAdapter

        // Setup bottom navigation
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

        // Connect the two navigation methods
        navView.notifyViewPager(viewPager)
        viewPager.notifyBottomNavigationView(navView)

        // Set default page
        navView.selectedItemId = R.id.navigation_roll

        // Observe the current connected user in order to respond to a sign out.
        observeSignOut()
    }

    /**
     * Update [viewPager] of any navigation via the bottom navigation view.
     */
    private fun BottomNavigationView.notifyViewPager(viewPager: ViewPager) =
        setOnNavigationItemSelectedListener(
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_roll -> {
                        viewPager.currentItem = 0
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_tales -> {
                        viewPager.currentItem = 1
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_profile -> {
                        viewPager.currentItem = 2
                        return@OnNavigationItemSelectedListener true
                    }
                    else -> viewPager.currentItem = 1
                }
                false
            }
        )

    /**
     * Update [bottomNavigationView] of any navigation via the view pager.
     */
    private fun ViewPager.notifyBottomNavigationView(bottomNavigationView: BottomNavigationView) =
        addOnPageChangeListener(
            object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {}
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    bottomNavigationView.menu.getItem(position).isChecked = true
                }
            }
        )

    /**
     * Observe the current connected user in order to respond to a sign out.
     */
    private fun observeSignOut() {

        // Observe "user" - if becomes null, the user signed out and we need to finish activity
        Authentication.currentUser.observe(this) {
            if (it == null) {

                // Disable auto sign in using Google smart lock (to avoid immediately re-logging in)
                Credentials.getClient(this).disableAutoSignIn()

                // Go back to splash screen
                startActivity(Intent(this, SplashActivity::class.java))
                finish()
            }
        }
    }
}