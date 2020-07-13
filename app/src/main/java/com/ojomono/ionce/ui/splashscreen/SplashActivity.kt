package com.ojomono.ionce.ui.splashscreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.firebase.ui.auth.util.ExtraConstants
import com.google.firebase.auth.ActionCodeSettings
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

                // Otherwise an error occurred
                // TODO check response.getError().getErrorCode() and handle the error.
                else Log.e(TAG, response.error.toString())

            }
        }
    }

    private fun startSignInActivity(){

        // Start building the sign-in Intent builder
        val signInIntentBuilder = AuthUI.getInstance().createSignInIntentBuilder()

        // If AuthUI can handle the intent (email link clicked), state it in the sign-in Intent
        if (AuthUI.canHandleIntent(intent)) {
            if (intent.extras == null) return
            val link = intent.data.toString()
            signInIntentBuilder.setEmailLink(link)
        }

        // Enable email link sign in
        val actionCodeSettings : ActionCodeSettings = ActionCodeSettings.newBuilder()
            .setAndroidPackageName(packageName, true, null)
            .setHandleCodeInApp(true) // This must be set to true
            .setUrl(Constants.DL_EMAIL_LINK_SIGN_IN) // This URL needs to be whitelisted
            .build()

        // TODO add phone, google, facebook and twitter login support
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder()
                .enableEmailLinkSignIn()
                .setActionCodeSettings(actionCodeSettings).build()//,
//            AuthUI.IdpConfig.PhoneBuilder().build(),
//            AuthUI.IdpConfig.GoogleBuilder().build(),
//            AuthUI.IdpConfig.FacebookBuilder().build(),
//            AuthUI.IdpConfig.TwitterBuilder().build()
        )

        // Create and launch sign-in intent
        startActivityForResult(
            signInIntentBuilder.setAvailableProviders(providers).build(),
            Constants.RC_SIGN_IN)
    }

    private fun startMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}