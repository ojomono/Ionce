package com.ojomono.ionce.ui.profile

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.LiveData
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException    // TODO avoid importing firebase packages here
import com.google.firebase.FirebaseTooManyRequestsException // TODO avoid importing firebase packages here
import com.google.firebase.auth.*   // TODO avoid importing firebase packages here
import com.ojomono.ionce.R
import com.ojomono.ionce.firebase.Authentication
import com.ojomono.ionce.utils.BaseViewModel
import com.ojomono.ionce.utils.TAG

class ProfileViewModel : BaseViewModel(), PopupMenu.OnMenuItemClickListener {
    // Current logged in user
    val user: LiveData<FirebaseUser?> = Authentication.currentUser  // TODO: Use a Repository class

    // User info of the different providers                         // TODO: Use a Repository class
    val googleUserInfo: LiveData<UserInfo> = Authentication.googleUserInfo
    val facebookUserInfo: LiveData<UserInfo> = Authentication.facebookUserInfo
    val twitterUserInfo: LiveData<UserInfo> = Authentication.twitterUserInfo
    val phoneUserInfo: LiveData<UserInfo> = Authentication.phoneUserInfo

    // Fields needed for linking with providers
    var phoneNumberToVerify: String = ""
    val facebookCallbackManager: CallbackManager = CallbackManager.Factory.create()
    lateinit var googleSignInClient: GoogleSignInClient

    // Types of supported events
    sealed class EventType() : Event {
        class ShowPopupMenu(val view: View) : EventType()
        object ShowImagePicker : EventType()
        object ShowEditNameDialog : EventType()
        object ShowTypePhoneDialog : EventType()
        object ShowLinkWithTwitter : EventType()
        object ShowLinkWithGoogle : EventType()
        class ShowProgressBar(val task: Task<*>) : EventType()
        class ShowErrorMessage(val messageResId: Int) : EventType()
    }

    /********************/
    /** Initialization **/
    /********************/

    // Refresh the user data (in case the name/photo/... was changed on another device)
    init {
        refresh()
    }

    /**********************/
    /** on click methods **/
    /**********************/

    // TODO onPictureClicked should open picture activity to view photo. Actions should be there.
    fun onSettingsClicked(view: View) = postEvent(EventType.ShowPopupMenu(view))
    fun onPictureClicked() = postEvent(EventType.ShowImagePicker)
    fun onNameClicked() = postEvent(EventType.ShowEditNameDialog)
    fun onPhoneClicked() = postEvent(EventType.ShowTypePhoneDialog)
    fun onTwitterClicked() = postEvent(EventType.ShowLinkWithTwitter)
    fun onGoogleClicked() = postEvent(EventType.ShowLinkWithGoogle)

    /**********************************************/
    /** MenuItem.OnMenuItemClickListener methods **/
    /**********************************************/

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_sign_out -> {
                Authentication.signOut()
                true
            }
            else -> false
        }
    }

    /*******************/
    /** logic methods **/
    /*******************/

    /**
     * Reload current user data.
     */
    fun refresh() = Authentication.reloadCurrentUser()

    /**
     * Get the callback object for the facebook button.
     */
    fun getFacebookCallback() = object : FacebookCallback<LoginResult> {
        override fun onSuccess(loginResult: LoginResult) {
            Log.d(TAG, "facebook:onSuccess:$loginResult")
            val task = handleFacebookAccessToken(loginResult.accessToken)
            if (task != null) postEvent(EventType.ShowProgressBar(task))    // Show progress bar
        }

        override fun onCancel() {
            Log.d(TAG, "facebook:onCancel")
        }

        override fun onError(error: FacebookException) {
            Log.d(TAG, "facebook:onError", error)
        }
    }

    /**
     * Get the intent for the image picker dialog.
     */
    fun getImagePickerIntent() =
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)

    /**
     * Update user photo to given [uri].
     */
    fun updateUserPicture(uri: Uri) = Authentication.updatePhotoUrl(uri, true)

    /**
     * Update user displayed name to given [name].
     */
    fun updateUserName(name: String) = Authentication.updateDisplayName(name)

    /**
     * Get the callback object for the phone verification process.
     */
    fun getPhoneVerificationCallbacks() =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
                Authentication.linkWithPhone(credential)
                phoneNumberToVerify = ""    // Clear flag
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)

                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        // Invalid request
                    }
                    is FirebaseTooManyRequestsException -> {
                        // The SMS quota for the project has been exceeded
                    }
                }

                postEvent(EventType.ShowErrorMessage(R.string.profile_phone_verify_failed_message))
                phoneNumberToVerify = ""    // Clear flag
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
//                var storedVerificationId = verificationId
//                var resendToken = token

                // ...
            }
        }

    /**
     * Connect current user with Facebook account with given [token].
     */
    fun handleFacebookAccessToken(token: AccessToken) = Authentication.linkWithFacebook(token)

    /**
     * Connect current user with Google account (token in [intent]).
     */
    fun handleGoogleResult(intent: Intent?) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        val googleSignInTask =
            GoogleSignIn.getSignedInAccountFromIntent(intent)
        try {
            // Google Sign In was successful, authenticate with Firebase
            googleSignInTask.getResult(ApiException::class.java)?.let { account ->
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                account.idToken?.let {
                    val task = Authentication.linkWithGoogle(it)
                    if (task != null) postEvent(EventType.ShowProgressBar(task))
                }
            }
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "Google sign in failed", e)
        }
    }
}