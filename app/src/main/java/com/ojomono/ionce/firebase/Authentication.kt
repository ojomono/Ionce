package com.ojomono.ionce.firebase

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.ojomono.ionce.utils.TAG

/**
 * Handles all interactions with Firebase authentication.
 */
object Authentication {

    // Dynamic Links
    private const val DL_EMAIL_LINK_SIGN_IN = "https://ionce.page.link"

    // The Firebase Authentication and it's pre-built UI instances
    private val authUI = AuthUI.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance().apply {
        // Update the LiveData every time the underlying token state changes.
        addAuthStateListener { _currentUser.value = currentUser }
    }

    // Current logged in user
    private val _currentUser: MutableLiveData<FirebaseUser?> =
        MutableLiveData<FirebaseUser?>().apply { value = firebaseAuth.currentUser }
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    /**
     * Build an intent that will open the FirebaseUI sign-in screen. If possible, enable email link
     * directing to the given [androidPackageName]. If the function is called after the email link
     * was clicked, the link should be given as the [activityIntent] model.
     */
    fun buildSignInIntent(
        androidPackageName: String? = null,
        activityIntent: Intent? = null
    ): Intent {

        // Start building the sign-in Intent and email builders
        val signInIntentBuilder = authUI.createSignInIntentBuilder()
        val emailBuilder = AuthUI.IdpConfig.EmailBuilder()

        // If the package name is given, we can enable email link sign-in
        if (!androidPackageName.isNullOrEmpty()) {

            // If the intent is given and AuthUI can handle it (email link clicked),
            // state it in the sign-in Intent
            if (activityIntent != null && AuthUI.canHandleIntent(activityIntent)) {
                val link = activityIntent.data.toString()
                signInIntentBuilder.setEmailLink(link)
            }

            // Enable email link sign in
            val actionCodeSettings: ActionCodeSettings =
                ActionCodeSettings.newBuilder()
                    .setAndroidPackageName(androidPackageName, true, null)
                    .setHandleCodeInApp(true) // This must be set to true
                    .setUrl(DL_EMAIL_LINK_SIGN_IN) // This URL needs to be whitelisted
                    .build()

            emailBuilder.enableEmailLinkSignIn().setActionCodeSettings(actionCodeSettings)
        }

        // Choose authentication providers
        val providers = arrayListOf(
            emailBuilder.build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build()
        )

        // Return the built intent
        return signInIntentBuilder.setAvailableProviders(providers).build()
    }

    /**
     * Handle the case of a successful sign-in.
     */
    fun handleSignInSucceeded() {
        // Get reference to current user's document from Firestore.
        Database.switchUserDocument(currentUser.value?.uid)
    }

    /**
     * Handle the case of a failed sign-in. Response should be given in [dataIntent].
     */
    fun handleSignInFailed(dataIntent: Intent?) {
        val response = IdpResponse.fromResultIntent(dataIntent)

        // If response is null the user canceled the sign-in flow using the back button.
        // Otherwise an error occurred:
        // Use response.error.errorCode if a specific error handling is needed.
        if (response != null) Log.e(TAG, response.error.toString())
    }

    /**
     * Update the current user's displayed name to [displayName].
     */
    fun updateDisplayName(displayName: String) {
        updateProfile(UserProfileChangeRequest.Builder().setDisplayName(displayName).build())
    }

    /**
     * Update the current user's photo to [photoUri].
     */
    fun updatePhotoUri(photoUri: Uri?) {
        updateProfile(UserProfileChangeRequest.Builder().setPhotoUri(photoUri).build())
    }

    /**
     * Send the given [profileUpdates] change request to Firebase.
     */
    private fun updateProfile(profileUpdates: UserProfileChangeRequest) {
        currentUser.value?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User profile updated.")
                    _currentUser.value = firebaseAuth.currentUser
                }
            }
    }

    /**
     * sign out of Firebase Authentication as well as all social identity providers. a [context] and
     * a [onCompleteListener] are needed.
     */
    fun signOut(context: Context, onCompleteListener: OnCompleteListener<Void>) {
        authUI.signOut(context).addOnCompleteListener(onCompleteListener)
    }
}