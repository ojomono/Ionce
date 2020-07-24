package com.ojomono.ionce.ui.splashscreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ojomono.ionce.MainActivity
import com.ojomono.ionce.R
import com.ojomono.ionce.utils.Constants
import com.ojomono.ionce.firebase.Authentication


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // If no user is logged in, open sign-in screen
        if (Authentication.getCurrentUser() == null)
            startActivityForResult(
                Authentication.buildSignInIntent(packageName, intent),
                Constants.RC_SIGN_IN
            )
        // Open main activity for the logged-in user
        else startMainActivity()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.RC_SIGN_IN) {

            if (resultCode == Activity.RESULT_OK) {

                // Successfully signed in
//                val user = Authentication.getCurrentUser()

                // Open home screen
                startMainActivity()

            } else {    // Sign in failed.
                Authentication.handleSignInFailed(data)
                finish()
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}