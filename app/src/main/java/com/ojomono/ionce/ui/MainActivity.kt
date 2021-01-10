package com.ojomono.ionce.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.auth.api.credentials.Credentials
import com.ojomono.ionce.R
import com.ojomono.ionce.firebase.Authentication
import com.ojomono.ionce.ui.profile.ProfileFragment
import com.ojomono.ionce.ui.roll.RollFragment
import com.ojomono.ionce.ui.tales.list.TalesFragment
import com.ojomono.ionce.ui.tales.list.TalesFragment.Companion.DEFAULT_COLUMN_COUNT

class MainActivity : AppCompatActivity() {

    /************/
    /** Fields **/
    /************/

    // Create the pager adapter
    private val rollFragment by lazy { RollFragment() }
    private val talesFragment by lazy { TalesFragment.newInstance(DEFAULT_COLUMN_COUNT) }
    private val profileFragment by lazy { ProfileFragment() }
    private val fragments: List<Fragment> = listOf(rollFragment, talesFragment, profileFragment)
    private val pagerAdapter: MainViewPagerAdapter by lazy {
        MainViewPagerAdapter(this, fragments)
    }

    // The pager widget, which handles animation and allows swiping horizontally to access previous
    // and next wizard steps.
    private lateinit var viewPager: ViewPager2
    private lateinit var navView: BottomNavigationView

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Instantiate a ViewPager2 and a PagerAdapter (to allow swiping between tabs)
        viewPager = findViewById(R.id.pager_view)
        viewPager.adapter = pagerAdapter

        // Setup bottom navigation
        navView = findViewById(R.id.nav_view)

        // Connect the two navigation methods
        navView.notifyViewPager(viewPager)
        viewPager.notifyBottomNavigationView(navView)

        // Set default page
        navView.selectedItemId = navView.menu.getItem(DEFAULT_PAGE).itemId

        // Observe the current connected user in order to respond to a sign out.
        observeSignOut()
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == DEFAULT_PAGE) {
            // If the user is currently looking at the default page, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the default page.
            viewPager.currentItem = DEFAULT_PAGE
        }
    }

    /*********************/
    /** private methods **/
    /*********************/

    /**
     * Update [viewPager] of any navigation via the bottom navigation view.
     */
    private fun BottomNavigationView.notifyViewPager(viewPager: ViewPager2) =
        setOnNavigationItemSelectedListener {
            val page = when (it.itemId) {
                R.id.navigation_roll -> 0
                R.id.navigation_tales -> 1
                R.id.navigation_profile -> 2
                else -> 0
            }

            if (page != viewPager.currentItem) viewPager.currentItem = page

            true
        }

    /**
     * Update [bottomNavigationView] of any navigation via the view pager.
     */
    private fun ViewPager2.notifyBottomNavigationView(bottomNavigationView: BottomNavigationView) =
        registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val navigation = bottomNavigationView.menu.getItem(position).itemId

                    if (bottomNavigationView.selectedItemId != navigation)
                        bottomNavigationView.selectedItemId = navigation
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

    /**
     * A pager adapter that represents the given [fragments] list.
     */
    private inner class MainViewPagerAdapter(
        activity: FragmentActivity,
        private val fragments: List<Fragment>
    ) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = NUM_PAGES
        override fun createFragment(position: Int): Fragment = fragments[position]
    }

    /***************/
    /** Constants **/
    /***************/

    companion object {

        // The number of pages
        private const val NUM_PAGES = 3

        // The default page
        private const val DEFAULT_PAGE = 0
    }

}