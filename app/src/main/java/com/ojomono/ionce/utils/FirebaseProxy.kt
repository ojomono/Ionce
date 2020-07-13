package com.ojomono.ionce.utils

import android.content.Intent
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.ActionCodeSettings

/**
 * Handles all interactions with Firebase.
 */
object FirebaseProxy : Tagged {

    /**
     * Build an intent that will open the FirebaseUI sign-in screen. If possible, enable email link
     * directing to the given [androidPackageName]. If the function is called after the email link
     * was clicked, the link should be given as the [activityIntent] data.
     */
    fun buildSignInIntent(
        androidPackageName : String? = null,
        activityIntent: Intent? = null
    ) : Intent {

        // Start building the sign-in Intent and email builders
        val signInIntentBuilder = AuthUI.getInstance().createSignInIntentBuilder()
        val emailBuilder = AuthUI.IdpConfig.EmailBuilder()

        // If the package name is given, we can enable email link sign-in
        if (!androidPackageName.isNullOrEmpty()){

            // If the intent is given and AuthUI can handle it (email link clicked),
            // state it in the sign-in Intent
            if (activityIntent != null && AuthUI.canHandleIntent(activityIntent)) {
                val link = activityIntent.data.toString()
                signInIntentBuilder.setEmailLink(link)
            }

            // Enable email link sign in
            val actionCodeSettings : ActionCodeSettings = ActionCodeSettings.newBuilder()
                .setAndroidPackageName(androidPackageName, true, null)
                .setHandleCodeInApp(true) // This must be set to true
                .setUrl(Constants.DL_EMAIL_LINK_SIGN_IN) // This URL needs to be whitelisted
                .build()

            emailBuilder.enableEmailLinkSignIn().setActionCodeSettings(actionCodeSettings)
        }

        // TODO add phone, google, facebook and twitter login support
        // Choose authentication providers
        val providers = arrayListOf(
            emailBuilder.build()//,
//            AuthUI.IdpConfig.PhoneBuilder().build(),
//            AuthUI.IdpConfig.GoogleBuilder().build(),
//            AuthUI.IdpConfig.FacebookBuilder().build(),
//            AuthUI.IdpConfig.TwitterBuilder().build()
        )

        // Return the built intent
        return signInIntentBuilder.setAvailableProviders(providers).build()
    }

    /**
     * Handle the case of a failed sign-in and returns if the cause is that the user canceled the
     * process (else an error had occurred). Response should be given in [dataIntent].
     */
    fun handleSignInFailed(dataIntent : Intent?) : Boolean {

        val response = IdpResponse.fromResultIntent(dataIntent)

        var isCancelled = false

        // If response is null the user canceled the sign-in flow using the back button.
        if (response == null) isCancelled = true
        // Otherwise an error occurred
        // TODO check response.getError().getErrorCode() and handle the error.
        else Log.e(TAG, response.error.toString())

        return isCancelled
    }

}