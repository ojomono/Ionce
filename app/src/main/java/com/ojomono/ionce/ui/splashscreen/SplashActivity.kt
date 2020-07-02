package com.ojomono.ionce.ui.splashscreen

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.ojomono.ionce.MainActivity
import com.ojomono.ionce.R
import com.ojomono.ionce.utils.BaseActivity
import com.ojomono.ionce.utils.Constants

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Open sign-in screen
        startSignInActivity()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {

                // Successfully signed in
//                val user = FirebaseAuth.getInstance().currentUser

                // Open home screen
                startMainActivity()

            } else {    // Sign in failed.

                // If response is null the user canceled the sign-in flow using the back button.
                if (response == null) finish()

                // Otherwise check response.getError().getErrorCode() and handle the error.
                else Log.e(TAG, response.error.toString())

            }
        }
    }

    private fun startSignInActivity(){

        // TODO add phone, google, facebook and twitter login support
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()//,
//            AuthUI.IdpConfig.PhoneBuilder().build(),
//            AuthUI.IdpConfig.GoogleBuilder().build(),
//            AuthUI.IdpConfig.FacebookBuilder().build(),
//            AuthUI.IdpConfig.TwitterBuilder().build()
        )

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            Constants.RC_SIGN_IN)

    }

    private fun startMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}