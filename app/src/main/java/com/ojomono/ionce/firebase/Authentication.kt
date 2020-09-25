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
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.ojomono.ionce.utils.TAG

/**
 * Handles all interactions with Firebase authentication.
 */
object Authentication {

    /***************/
    /** Constants **/
    /***************/

    // Dynamic Links
    private const val DL_EMAIL_LINK_SIGN_IN = "https://ionce.page.link"

    /************/
    /** Fields **/
    /************/

    // The Firebase Authentication and it's pre-built UI instances
    private val authUI = AuthUI.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance().apply {
        // Update the LiveData every time the underlying token state changes.
        addAuthStateListener { _currentUser.value = currentUser }
    }

    // Current logged in user
    private val _currentUser: MutableLiveData<FirebaseUser?> =
        MutableLiveData<FirebaseUser?>().apply {
            value = firebaseAuth.currentUser
        }
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    /*********************/
    /** Sign in methods **/
    /*********************/

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

    /***********************************/
    /** Invoke firebase tasks methods **/
    /***********************************/

    /**
     * Refresh the data of the current user, and return the refreshing [Task].
     */
    fun reloadCurrentUser(): Task<Void>? {
        return firebaseAuth.currentUser?.reload()
            ?.addOnCompleteListener { _currentUser.value = firebaseAuth.currentUser }
    }

    /**
     * Update the current user's photo to [photoUri], and return the updating [Task].
     */
    fun updatePhotoUri(photoUri: Uri): Task<Void>? {
        var updateProfileTask: Task<Void>? = null
        currentUser.value?.uid?.let {
            updateProfileTask =
                Storage.updateUserPhoto(it, photoUri)
                    .continueWithTask { downloadUrlTask ->
                        if (downloadUrlTask.isSuccessful) {
                            updateProfile(
                                UserProfileChangeRequest.Builder()
                                    .setPhotoUri(downloadUrlTask.result)
                                    .build()
                            )
                        } else {
                            // Handle failures and return null task
                            Log.e(TAG, downloadUrlTask.exception.toString())
                            null
                        }
                    }
        }
        return updateProfileTask
    }

    /**
     * Update the current user's displayed name to [displayName], and return the updating [Task].
     */
    fun updateDisplayName(displayName: String): Task<Void>? {
        return updateProfile(UserProfileChangeRequest.Builder().setDisplayName(displayName).build())
    }

    /**
     * Update the current user's email to [email], and return the updating [Task].
     */
    fun updateEmail(email: String): Task<Void>? {
        return currentUser.value?.updateEmail(email)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User email address updated.")
                } else {
                    Log.e(TAG, task.exception.toString())
                    // TODO: Re-authenticate the user when feature is ready:
                    //  https://github.com/firebase/FirebaseUI-Android/issues/563#issuecomment-367736441
                    //  Also send a verification email and implement password change and delete account options:
                    //  https://firebase.google.com/docs/auth/android/manage-users#re-authenticate_a_user
                }
            }
    }

    /**
     * sign out of Firebase Authentication as well as all social identity providers, and return the
     * sign out [Task].
     */
    fun signOut(context: Context): Task<Void> {
        return authUI.signOut(context)
    }

    /*********************/
    /** Private methods **/
    /*********************/

    /**
     * Send the given [profileUpdates] change request to Firebase, and return the updating [Task].
     */
    private fun updateProfile(profileUpdates: UserProfileChangeRequest): Task<Void>? {
        return currentUser.value?.updateProfile(profileUpdates)
            ?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "User profile updated.")
                    _currentUser.value = firebaseAuth.currentUser
                }
            }
    }
}