package com.ojomono.ionce

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.credentials.Credentials
import com.ojomono.ionce.firebase.Authentication

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

        // Observe the current connected user in order to respond to a sign out.
        observeSignOut()
    }

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