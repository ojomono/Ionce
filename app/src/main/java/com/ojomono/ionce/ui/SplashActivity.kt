package com.ojomono.ionce.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.*
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.ojomono.ionce.BuildConfig
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.ActivitySplashBinding
import com.ojomono.ionce.firebase.Authentication
import com.ojomono.ionce.firebase.Authentication.handleCollision
import com.ojomono.ionce.firebase.Conversions
import com.ojomono.ionce.utils.TAG
import com.ojomono.ionce.utils.withProgressBar

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private val authUI = AuthUI.getInstance()

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View binding
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // If no user is logged in, open sign-in screen
        if (Authentication.currentUser.value == null)
            startActivityForResult(buildSignInIntent(intent), RC_SIGN_IN)
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
                handleSignInSucceeded(data)

                // Open home screen
                startMainActivity()

            } else {

                // Sign in failed.
                handleSignInFailed(data)

                // Close app
                finish()

            }
        }
    }

    /***********************/
    /** private methods **/
    /***********************/

    /**
     * Build an intent that will open the FirebaseUI sign-in screen. Enable email link directing to
     * the APPLICATION_ID (main package). If the function is called after the email link
     * was clicked, the link should be given as the [activityIntent] model.
     */
    private fun buildSignInIntent(activityIntent: Intent? = null): Intent {

        // Start building the sign-in Intent and email builders
        val signInIntentBuilder = authUI.createSignInIntentBuilder()
        val emailBuilder = AuthUI.IdpConfig.EmailBuilder()

        // If the intent is given and AuthUI can handle it (email link clicked),
        // state it in the sign-in Intent
        if (activityIntent != null && AuthUI.canHandleIntent(activityIntent)) {
            val link = activityIntent.data.toString()
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
            .setTheme(R.style.AppTheme_SignInScreen) // Set theme
            .build()
    }

    private fun handleSignInSucceeded(dataIntent: Intent?) {
        val response = IdpResponse.fromResultIntent(dataIntent)

        // For new created users, check if a photo is available from the auth provider
        if (response?.isNewUser == true) {
            // All users have a default FirebaseAuth provider data - we want to check which is the
            // other one
            Authentication.currentUser.value?.providerData
                ?.find { it.providerId != FirebaseAuthProvider.PROVIDER_ID }?.apply {
                    val photoUrl = when (providerId) {
                        GoogleAuthProvider.PROVIDER_ID ->
                            // Replace "s96-c" to "s400-c" to get 400x400 image
                            photoUrl.toString().replace(
                                PU_GOOGLE_SIZE_COMPONENT.format(PU_GOOGLE_DEFAULT_SIZE),
                                PU_GOOGLE_SIZE_COMPONENT.format(PU_WANTED_SIZE)
                            )
                        FacebookAuthProvider.PROVIDER_ID ->
                            // TODO: Get facebook link (when Graph API will allow it again)
                            // Add parameters: "?height=400&access_token=${response.idpToken}" to
                            // get 400x400 image
                            photoUrl.toString().plus(
                                PU_FACEBOOK_SIZE_COMPONENT.format(PU_WANTED_SIZE, response.idpToken)
                            )
                        TwitterAuthProvider.PROVIDER_ID ->
                            // TODO: Get Twitter screen_name (when getting Facebook link)
                            // Replace "_normal" to "_400x400" to get 400x400 image
                            photoUrl.toString().replace(
                                PU_TWITTER_SIZE_COMPONENT_DEFAULT,
                                PU_TWITTER_SIZE_COMPONENT_WANTED
                                    .format(PU_WANTED_SIZE, PU_WANTED_SIZE)
                            )
                        else -> null
                    }
                    photoUrl?.let { Authentication.updatePhotoUrl(it.toUri()) }
                }
        }
    }

    private fun handleSignInFailed(dataIntent: Intent?) {
        val response = IdpResponse.fromResultIntent(dataIntent)

        // If response is null the user canceled the sign-in flow using the back button.
        // Otherwise an error occurred:
        // Use response.error.errorCode if a specific error handling is needed.
        if (response != null) Log.e(TAG, response.error.toString())
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
                        Authentication.linkWithEmail(deepLink)?.withProgressBar(binding.progressBar)
                            ?.handleCollision {
                                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                            }
                }
            }
            .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }

    private fun startMainActivity() {

        // TODO remove call to fixUserPhotoUriPathIfNeeded when all user photos has converted ==
        //  When 'images/' folder in Firestore Storage will get empty.
        // In v1.0.1, user photos were saved in the path: 'imaged/<UID>'. Fix that.
        Authentication.currentUser.value?.let { Conversions.fixUserPhotoPathInStorageIfNeeded(it) }

        startActivity(Intent(this, MainActivity::class.java))
        finish()    // Avoid coming back here if user presses 'back'
    }

    /***************/
    /** Constants **/
    /***************/

    companion object {
        // Request codes
        const val RC_SIGN_IN = 1

        // Photo Url Size
        private const val PU_WANTED_SIZE = 400  // Not all values are supported by Twitter API!
        private const val PU_GOOGLE_DEFAULT_SIZE = 96
        private const val PU_GOOGLE_SIZE_COMPONENT = "s%d-c"
        private const val PU_FACEBOOK_SIZE_COMPONENT = "?height=%d&access_token=%s"
        private const val PU_TWITTER_SIZE_COMPONENT_DEFAULT = "_normal"
        private const val PU_TWITTER_SIZE_COMPONENT_WANTED = "_%dx%d"
    }
}