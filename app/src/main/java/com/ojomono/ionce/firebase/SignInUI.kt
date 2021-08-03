package com.ojomono.ionce.firebase

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.ActionCodeSettings
import com.ojomono.ionce.BuildConfig
import com.ojomono.ionce.R

/**
 * Handles all interactions with Firebase UI.
 */
object SignInUI {

    val authResultContract = object : ActivityResultContract<Intent, IdpResponse>() {

        /**
         * Build an intent that will open the FirebaseUI sign-in screen. Enable email link directing to
         * the APPLICATION_ID (main package). If the function is called after the email link
         * was clicked, the link should be given as the [input] model.
         */
        override fun createIntent(context: Context, input: Intent?): Intent {

            // Start building the sign-in Intent and email builders
            val signInIntentBuilder = AuthUI.getInstance().createSignInIntentBuilder()
            val emailBuilder = AuthUI.IdpConfig.EmailBuilder()

            // If the intent is given and AuthUI can handle it (email link clicked),
            // state it in the sign-in Intent
            if (input != null && AuthUI.canHandleIntent(input)) {
                val link = input.data.toString()
                signInIntentBuilder.setEmailLink(link)
            }

            // Enable email link sign in
            val actionCodeSettings: ActionCodeSettings =
                ActionCodeSettings.newBuilder()
                    .setAndroidPackageName(BuildConfig.APPLICATION_ID, true, null)
                    .setHandleCodeInApp(true) // This must be set to true
                    .setUrl(Authentication.DL_URL_DOMAIN + Authentication.DL_FINISH_SIGN_UP) // This URL needs to be whitelisted
                    .build()
            emailBuilder.enableEmailLinkSignIn().setActionCodeSettings(actionCodeSettings)

            // Choose authentication providers
            val providers = arrayListOf(
                AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.FacebookBuilder().build(),
                AuthUI.IdpConfig.TwitterBuilder().build(),
                emailBuilder.build(),
                AuthUI.IdpConfig.PhoneBuilder().build()
            )

            // Return the built intent
            return signInIntentBuilder.setAvailableProviders(providers)
                .setLogo(R.drawable.app_logo) // Set logo drawable
                .setTheme(R.style.AppTheme) // Set theme
                .build()
        }

        /**
         * Return IdpResponse if activity result was ok (not canceled).
         */
        override fun parseResult(resultCode: Int, intent: Intent?) = when (resultCode) {
            Activity.RESULT_OK -> IdpResponse.fromResultIntent(intent)
            else -> null
        }

    }
}