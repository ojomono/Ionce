package com.ojomono.ionce.firebase

// TODO: Avoid Android imports and move to separated module when needed for more UI platforms
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.facebook.AccessToken
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.actionCodeSettings
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
    const val DL_URL_DOMAIN = "https://ionce.page.link"
    const val DL_FINISH_SIGN_UP = "/finishSignUp"
    private const val DL_LINK_WITH_EMAIL = "/linkWithEmail"

    /************/
    /** Fields **/
    /************/

    // The Firebase Authentication
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
        MutableLiveData<FirebaseUser?>().apply { value = auth.currentUser }
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    // Current user's providers data
    val googleUserInfo: LiveData<UserInfo?> =
        currentUser.map {
            it?.providerData?.find { data -> data.providerId == GoogleAuthProvider.PROVIDER_ID }
        }
    val facebookUserInfo: LiveData<UserInfo?> =
        currentUser.map {
            it?.providerData?.find { data -> data.providerId == FacebookAuthProvider.PROVIDER_ID }
        }
    val twitterUserInfo: LiveData<UserInfo?> =
        currentUser.map {
            it?.providerData?.find { data -> data.providerId == TwitterAuthProvider.PROVIDER_ID }
        }
    val emailUserInfo: LiveData<UserInfo?> =
        currentUser.map {
            it?.providerData?.find { data -> data.providerId == EmailAuthProvider.PROVIDER_ID }
        }
    val phoneUserInfo: LiveData<UserInfo?> =
        currentUser.map {
            it?.providerData?.find { data -> data.providerId == PhoneAuthProvider.PROVIDER_ID }
        }

    /***********************************/
    /** Invoke firebase tasks methods **/
    /***********************************/

    /**
     * Refresh the data of the current user, and return the refreshing [Task].
     */
    fun reloadCurrentUser(): Task<Void> {
        return auth.currentUser?.reload()
            ?.addOnCompleteListener { _currentUser.value = auth.currentUser }
            ?: throw Utils.NoSignedInUserException
    }

    /**
     * Update the current user's photo to [photoUrl], and return the updating [Task].
     */
    fun updatePhotoUrl(photoUrl: Uri?): Task<Void> =
        updateProfile(UserProfileChangeRequest.Builder().setPhotoUri(photoUrl).build())

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
    fun sendSignInLinkToEmail(email: String, packageName: String): Task<Void> =
        if (email.isNotEmpty()) {
            emailToVerify = email   // Store for use when actually linking the email
            val actionCodeSettings = actionCodeSettings {
                url = DL_URL_DOMAIN + DL_LINK_WITH_EMAIL
                handleCodeInApp = true  // This must be true
                setAndroidPackageName(packageName, true, null)
            }
            auth.sendSignInLinkToEmail(email, actionCodeSettings)
                .addOnCompleteListener { if (it.isSuccessful) Log.d(TAG, "Email sent.") }
        } else throw IllegalStateException("Email cannot be null")

    /**
     * Link the current user to email address.
     */
    fun linkWithEmail(emailLink: String) =
        if (emailToVerify.isNotEmpty()) {
            linkWithCredential(EmailAuthProvider.getCredentialWithLink(emailToVerify, emailLink))
                .addOnCompleteListener { emailToVerify = ""  /* Clear field */ }
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
            } ?: throw Utils.NoSignedInUserException

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
    private fun updateProfile(profileUpdates: UserProfileChangeRequest): Task<Void> {
        return currentUser.value?.updateProfile(profileUpdates)
            ?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "User profile updated.")
                    _currentUser.value = auth.currentUser
                }
            } ?: throw Utils.NoSignedInUserException
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
        } ?: throw Utils.NoSignedInUserException

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