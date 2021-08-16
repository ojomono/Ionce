package com.ojomono.ionce.firebase

import android.content.Intent
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.ActionCodeSettings
import com.ojomono.ionce.BuildConfig
import com.ojomono.ionce.R

/**
 * Handles all interactions with Firebase UI.
 */
object SignInUI {

    // Constants
    // TODO use string recourses
    private const val TOS_URL = "https://i-once.flycricket.io/terms.html"
    private const val PRIVACY_POLICY_URL = "https://i-once.flycricket.io/privacy.html"

    val authResultContract = FirebaseAuthUIActivityResultContract()

    /**
     * Build an intent that will open the FirebaseUI sign-in screen. Enable email link directing to
     * the APPLICATION_ID (main package). If the function is called after the email link
     * was clicked, the link should be given as the [input] model.
     */
    fun buildSignInIntent(input: Intent?): Intent {

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
            .setLogo(R.drawable.app_logo)
            .setTheme(R.style.AppTheme)
            .setTosAndPrivacyPolicyUrls(TOS_URL, PRIVACY_POLICY_URL)
            .build()
    }

}