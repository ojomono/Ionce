package com.ojomono.ionce.ui.splashscreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ojomono.ionce.MainActivity
import com.ojomono.ionce.R
import com.ojomono.ionce.utils.Constants
import com.ojomono.ionce.utils.FirebaseProxy


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // If no user is logged in, open sign-in screen
        if (FirebaseProxy.getCurrentUser() == null)
            startActivityForResult(
                FirebaseProxy.buildSignInIntent(packageName, intent),
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
//                val user = FirebaseProxy.getCurrentUser()

                // Open home screen
                startMainActivity()

            } else if (FirebaseProxy.handleSignInFailed(data)) finish() // Sign in failed.
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}