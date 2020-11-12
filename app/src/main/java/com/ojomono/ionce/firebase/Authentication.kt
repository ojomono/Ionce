package com.ojomono.ionce.firebase

// TODO: Avoid Android imports and move to separated module when needed for more UI platforms
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.facebook.AccessToken
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.actionCodeSettings
import com.ojomono.ionce.BuildConfig
import com.ojomono.ionce.R
import com.ojomono.ionce.utils.TAG


/**
 * Handles all interactions with Firebase authentication.
 */
object Authentication {

    /***************/
    /** Constants **/
    /***************/

    // Dynamic Links
    private const val DL_URL_DOMAIN = "https://ionce.page.link"
    private const val DL_FINISH_SIGN_UP = "/finishSignUp"
    private const val DL_LINK_WITH_EMAIL = "/linkWithEmail"

    // Photo Url Size
    private const val PU_WANTED_SIZE = 400  // Not all values are supported by Twitter API!
    private const val PU_GOOGLE_DEFAULT_SIZE = 96
    private const val PU_GOOGLE_SIZE_COMPONENT = "s%d-c"
    private const val PU_FACEBOOK_SIZE_COMPONENT = "?height=%d&access_token=%s"
    private const val PU_TWITTER_SIZE_COMPONENT_DEFAULT = "_normal"
    private const val PU_TWITTER_SIZE_COMPONENT_WANTED = "_%dx%d"

    /************/
    /** Fields **/
    /************/

    // TODO move AuthUI to SplashActivity to avoid importing Intent here
    // The Firebase Authentication and it's pre-built UI instances
    private val authUI = AuthUI.getInstance()
    private val auth = FirebaseAuth.getInstance().apply {
        // Update the LiveData every time the underlying token state changes.
        addAuthStateListener { _currentUser.value = currentUser }
    }

    // Email to verify for link authentication
    private var emailToVerify = ""

    // TODO replace liveData s with callbackFlows / StateFlows when they become non-experimental
    //  https://medium.com/firebase-tips-tricks/how-to-use-kotlin-flows-with-firestore-6c7ee9ae12f3
    // Current logged in user
    private val _currentUser: MutableLiveData<FirebaseUser?> =
        MutableLiveData<FirebaseUser?>().apply {
            value = auth.currentUser
        }
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    // Current user's providers data
    val googleUserInfo: LiveData<UserInfo> =
        Transformations.map(currentUser) {
            it?.providerData?.find { data -> data.providerId == GoogleAuthProvider.PROVIDER_ID }
        }
    val facebookUserInfo: LiveData<UserInfo> =
        Transformations.map(currentUser) {
            it?.providerData?.find { data -> data.providerId == FacebookAuthProvider.PROVIDER_ID }
        }
    val twitterUserInfo: LiveData<UserInfo> =
        Transformations.map(currentUser) {
            it?.providerData?.find { data -> data.providerId == TwitterAuthProvider.PROVIDER_ID }
        }
    val emailUserInfo: LiveData<UserInfo> =
        Transformations.map(currentUser) {
            it?.providerData?.find { data -> data.providerId == EmailAuthProvider.PROVIDER_ID }
        }
    val phoneUserInfo: LiveData<UserInfo> =
        Transformations.map(currentUser) {
            it?.providerData?.find { data -> data.providerId == PhoneAuthProvider.PROVIDER_ID }
        }

    /*********************/
    /** Sign in methods **/
    /*********************/
    // TODO this section will probably move to SplashActivity

    /**
     * Build an intent that will open the FirebaseUI sign-in screen. Enable email link directing to
     * the APPLICATION_ID (main package). If the function is called after the email link
     * was clicked, the link should be given as the [activityIntent] model.
     */
    fun buildSignInIntent(activityIntent: Intent? = null): Intent {

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
                .setUrl(DL_URL_DOMAIN + DL_FINISH_SIGN_UP) // This URL needs to be whitelisted
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
     * Handle the case of a successful sign-in.
     */
    fun handleSignInSucceeded(dataIntent: Intent?) {
        val response = IdpResponse.fromResultIntent(dataIntent)

        // Get reference to current user's document from Firestore.
        Database.switchUserDocument(currentUser.value?.uid)

        // For new created users, check if a photo is available from the auth provider
        if (response?.isNewUser == true) {
            // All users have a default FirebaseAuth provider data - we want to check which is the
            // other one
            currentUser.value?.providerData
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
                    photoUrl?.let { updatePhotoUrl(it.toUri(), false) }
                }
        }
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
        return auth.currentUser?.reload()
            ?.addOnCompleteListener { _currentUser.value = auth.currentUser }
    }

    /**
     * Update the current user's photo to [photoUrl], and return the updating [Task]. If needed,
     * ([uploadToStorage]) upload photo to [Storage].
     */
    fun updatePhotoUrl(photoUrl: Uri, uploadToStorage: Boolean = true): Task<Void>? {
        val profileUpdates = UserProfileChangeRequest.Builder()

        return if (uploadToStorage)
            currentUser.value?.uid?.let {
                Storage.uploadUserPhoto(it, photoUrl)
                    .continueWithTask { downloadUrlTask ->
                        if (downloadUrlTask.isSuccessful) {
                            updateProfile(
                                profileUpdates.setPhotoUri(downloadUrlTask.result).build()
                            )
                        } else {
                            // Handle failures and return null task
                            Log.e(TAG, downloadUrlTask.exception.toString())
                            null
                        }
                    }
            }
        else updateProfile(profileUpdates.setPhotoUri(photoUrl).build())
    }

    /**
     * Update the current user's displayed name to [displayName], and return the updating [Task].
     */
    fun updateDisplayName(displayName: String) =
        updateProfile(UserProfileChangeRequest.Builder().setDisplayName(displayName).build())

    // TODO: Allow email update when Re-authenticate feature in AuthUI is ready:
    //  https://github.com/firebase/FirebaseUI-Android/issues/563#issuecomment-367736441
    //  Also send a verification email and implement password change and delete account options:
    //  https://firebase.google.com/docs/auth/android/manage-users#re-authenticate_a_user

    /**
     * Send a sign-in link to the given [email].
     */
    fun sendSignInLinkToEmail(email: String): Task<Void>? {
        emailToVerify = email   // Store for use when actually linking the email
        val actionCodeSettings = actionCodeSettings {
            url = DL_URL_DOMAIN + DL_LINK_WITH_EMAIL
            handleCodeInApp = true  // This must be true
            setAndroidPackageName(BuildConfig.APPLICATION_ID, true, null)
        }
        return auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) Log.d(TAG, "Email sent.")
            }
    }

    /**
     * Link the current user to email address.
     */
    fun linkWithEmail(emailLink: String) =
        if (emailToVerify.isNotEmpty()) {
            linkWithCredential(EmailAuthProvider.getCredentialWithLink(emailToVerify, emailLink))
                ?.addOnCompleteListener { emailToVerify = ""  /* Clear field */ }
        } else null

    /**
     * Link the current user to phone number.
     */
    fun linkWithPhone(credential: AuthCredential) = linkWithCredential(credential)
    fun linkWithPhone(verificationId: String, code: String) =
        linkWithCredential(PhoneAuthProvider.getCredential(verificationId, code))

    /**
     * Link the current user to Facebook account.
     */
    fun linkWithFacebook(token: AccessToken) =
        linkWithCredential(FacebookAuthProvider.getCredential(token.token))

    /**
     * Link the current user to Google account.
     */
    fun linkWithGoogle(googleIdToken: String) =
        linkWithCredential(GoogleAuthProvider.getCredential(googleIdToken, null))

    /**
     * Unlink the current user from the given [providerNameResId].
     */
    fun unlinkProvider(providerNameResId: Int) =
        auth.currentUser?.unlink(getProviderId(providerNameResId))
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) _currentUser.value = task.result?.user
            }

    /**
     * sign out of Firebase Authentication as well as all social identity providers.
     */
    fun signOut() = auth.signOut()

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
                    _currentUser.value = auth.currentUser
                }
            }
    }

    /**
     * Link the current user to the given [credential].
     */
    private fun linkWithCredential(credential: AuthCredential) =
        auth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "linkWithCredential:success")
                _currentUser.value = task.result?.user
            } else Log.w(TAG, "linkWithCredential:failure", task.exception)
            // TODO merge accounts in case of collision.
        }

    /**
     * Get the provider id matching the given [providerNameResId].
     */
    private fun getProviderId(providerNameResId: Int) =
        when (providerNameResId) {
            R.string.profile_google_provider_name -> GoogleAuthProvider.PROVIDER_ID
            R.string.profile_facebook_provider_name -> FacebookAuthProvider.PROVIDER_ID
            R.string.profile_twitter_provider_name -> TwitterAuthProvider.PROVIDER_ID
            R.string.profile_email_provider_name -> EmailAuthProvider.PROVIDER_ID
            R.string.profile_phone_provider_name -> PhoneAuthProvider.PROVIDER_ID
            else -> ""
        }

    /****************/
    /** Extensions **/
    /****************/

    /**
     * Handle the FirebaseAuthUserCollisionException using the given [handlerFunc].
     */
    fun Task<AuthResult>.handleCollision(handlerFunc: (Exception) -> Unit) =
        apply {
            addOnFailureListener { if (it is FirebaseAuthUserCollisionException) handlerFunc(it) }
        }

}