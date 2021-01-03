package com.ojomono.ionce.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.ojomono.ionce.R
import com.ojomono.ionce.firebase.Authentication
import com.ojomono.ionce.firebase.Authentication.handleCollision
import com.ojomono.ionce.utils.TAG
import com.ojomono.ionce.utils.withProgressBar
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // If no user is logged in, open sign-in screen
        if (Authentication.currentUser.value == null)
            startActivityForResult(Authentication.buildSignInIntent(intent), RC_SIGN_IN)
        else {
            // Check for any other dynamic link
            handleDynamicLinks()

            // Open main activity
            startMainActivity()
        }
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

    private fun handleDynamicLinks() =
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                if (pendingDynamicLinkData != null) {
                    // Right now, app supports only one kind of link - link user with email
                    val deepLink = pendingDynamicLinkData.link.toString()
                    if (deepLink.isNotEmpty())
                        Authentication.linkWithEmail(deepLink)?.withProgressBar(progress_bar)
                            ?.handleCollision {
                                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                            }
                }
            }
            .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()    // Avoid coming back here if user presses 'back'
    }

    companion object {
        // Request codes
        const val RC_SIGN_IN = 1
    }
}