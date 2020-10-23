package com.ojomono.ionce

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ojomono.ionce.firebase.Authentication

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onStart() {
        super.onStart()

        // If no user is logged in, open sign-in screen
        if (Authentication.currentUser.value == null)
            startActivityForResult(
                Authentication.buildSignInIntent(packageName, intent),
                RC_SIGN_IN
            )
        // Open main activity for the logged-in user
        else startMainActivity()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

            if (resultCode == Activity.RESULT_OK) {

                // Successfully signed in
                Authentication.handleSignInSucceeded(data)

                // Open home screen
                startMainActivity()

            } else {

                // Sign in failed.
                Authentication.handleSignInFailed(data)

                // Close app
                finish()

            }
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()    // Avoid coming back here if user presses 'back'
    }

    companion object {
        // Request codes
        const val RC_SIGN_IN = 1
    }
}