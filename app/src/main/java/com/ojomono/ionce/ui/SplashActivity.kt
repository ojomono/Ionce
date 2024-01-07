package com.ojomono.ionce.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.*
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.ojomono.ionce.databinding.ActivitySplashBinding
import com.ojomono.ionce.firebase.Authentication
import com.ojomono.ionce.firebase.Authentication.handleCollision
import com.ojomono.ionce.firebase.SignInUI
import com.ojomono.ionce.utils.TAG
import com.ojomono.ionce.utils.withProgressBar

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private val authResultLauncher =
        registerForActivityResult(SignInUI.authResultContract) { res ->
            handleAuthResponse(res.idpResponse)
        }

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
            authResultLauncher.launch(SignInUI.buildSignInIntent(intent, packageName))

        // If a user is already logged in, check dynamic links and open home screen
        else {
            // Check for any other dynamic link
            handleDynamicLinks()

            // Open main activity
            startMainActivity()
        }
    }

    /***********************/
    /** private methods **/
    /***********************/

    private fun handleAuthResponse(idpResponse: IdpResponse?) =
        when {
            // If response is null the user canceled the sign-in flow using the back button.
            (idpResponse == null) -> finish()   // Close app.

            // Handle error from returned data.
            (idpResponse.error != null) -> idpResponse.error?.let { handleSignInFailed(it) }

            // Handle sign-in success from returned data.
            else -> handleSignInSucceeded(idpResponse)
        }

    private fun handleSignInFailed(error: Exception) {

        // Use error.errorCode if a specific error handling is needed.
        Log.e(TAG, error.toString())

        // Close app
        finish()
    }

    private fun handleSignInSucceeded(idpResponse: IdpResponse?) {

        // For new created users, check if a photo is available from the auth provider
        if (idpResponse?.isNewUser == true) {
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
                                PU_FACEBOOK_SIZE_COMPONENT.format(
                                    PU_WANTED_SIZE,
                                    idpResponse.idpToken
                                )
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

        // Open home screen
        startMainActivity()
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
        startActivity(Intent(this, MainActivity::class.java))
        finish()    // Avoid coming back here if user presses 'back'
    }

    /***************/
    /** Constants **/
    /***************/

    companion object {
        // Photo Url Size
        private const val PU_WANTED_SIZE = 400  // Not all values are supported by Twitter API!
        private const val PU_GOOGLE_DEFAULT_SIZE = 96
        private const val PU_GOOGLE_SIZE_COMPONENT = "s%d-c"
        private const val PU_FACEBOOK_SIZE_COMPONENT = "?height=%d&access_token=%s"
        private const val PU_TWITTER_SIZE_COMPONENT_DEFAULT = "_normal"
        private const val PU_TWITTER_SIZE_COMPONENT_WANTED = "_%dx%d"
    }
}