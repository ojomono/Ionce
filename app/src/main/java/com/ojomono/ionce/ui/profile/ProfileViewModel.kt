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
    val emailUserInfo: LiveData<UserInfo> = Authentication.emailUserInfo
    val phoneUserInfo: LiveData<UserInfo> = Authentication.phoneUserInfo
    val twitterUserInfo: LiveData<UserInfo> = Authentication.twitterUserInfo
    val facebookUserInfo: LiveData<UserInfo> = Authentication.facebookUserInfo
    val googleUserInfo: LiveData<UserInfo> = Authentication.googleUserInfo

    // Fields needed for linking with providers
    var phoneNumberToVerify: String = ""
    var storedVerificationId: String = ""
    val facebookCallbackManager: CallbackManager = CallbackManager.Factory.create()
    lateinit var googleSignInClient: GoogleSignInClient

    // Types of supported events
    sealed class EventType() : Event {
        class ShowPopupMenu(val view: View) : EventType()
        object ShowImagePicker : EventType()
        object ShowNameEditDialog : EventType()
        object ShowEmailAddressDialog : EventType()
        object ShowPhoneNumberDialog : EventType()
        object ShowVerificationCodeDialog : EventType()
        object ShowLinkWithTwitter : EventType()
        object ShowLinkWithFacebook : EventType()
        object ShowLinkWithGoogle : EventType()
        class ShowUnlinkProviderDialog(val providerNameResId: Int) : EventType()
    }

    /********************/
    /** Initialization **/
    /********************/

    init {
        // Refresh the user data (in case the name/photo/... was changed on another device)
        refresh()
    }

    /**********************/
    /** on click methods **/
    /**********************/

    // TODO onPictureClicked should open picture activity to view photo. Actions should be there.
    fun onSettingsClicked(view: View) = postEvent(EventType.ShowPopupMenu(view))
    fun onPictureClicked() = postEvent(EventType.ShowImagePicker)
    fun onNameClicked() = postEvent(EventType.ShowNameEditDialog)

    /**
     * Handler method for all Providers shown on screen. Determine the right event according to the
     * clicked [view] id, and whether or not the matching userInfo exists (provider linked).
     */
    fun onProviderClicked(view: View) =
        when (view.id) {
            R.id.linear_email_provider ->
                if (emailUserInfo.value == null) EventType.ShowEmailAddressDialog
                else EventType.ShowUnlinkProviderDialog(R.string.profile_email_provider_name)
            R.id.linear_phone_provider ->
                if (phoneUserInfo.value == null) EventType.ShowPhoneNumberDialog
                else EventType.ShowUnlinkProviderDialog(R.string.profile_phone_provider_name)
            R.id.linear_twitter_provider ->
                if (twitterUserInfo.value == null) EventType.ShowLinkWithTwitter
                else EventType.ShowUnlinkProviderDialog(R.string.profile_twitter_provider_name)
            R.id.linear_facebook_provider ->
                if (facebookUserInfo.value == null) EventType.ShowLinkWithFacebook
                else EventType.ShowUnlinkProviderDialog(R.string.profile_facebook_provider_name)
            R.id.linear_google_provider ->
                if (googleUserInfo.value == null) EventType.ShowLinkWithGoogle
                else EventType.ShowUnlinkProviderDialog(R.string.profile_google_provider_name)
            else -> null    // If an error is needed - use BaseViewModel's "showErrorMessage"
        }?.let { postEvent(it) }

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
            handleFacebookAccessToken(loginResult.accessToken)?.withProgressBar()
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
    fun updateUserPicture(uri: Uri) =
        Authentication.updatePhotoUrl(uri, true)?.withProgressBar()

    /**
     * Update user displayed name to given [name].
     */
    fun updateUserName(name: String) =
        Authentication.updateDisplayName(name)?.withProgressBar()

    /**
     * Send a sign-in link to the given [email].
     */
    fun sendSignInLinkToEmail(email: String) =
        Authentication.sendSignInLinkToEmail(email)?.withProgressBar()

    /**
     * Get the callback object for the phone verification process.
     */
    fun getPhoneVerificationCallbacks() =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
                Authentication.linkWithPhone(credential)?.withProgressBar()
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

                showErrorMessage(R.string.profile_phone_verify_failed_message)
                phoneNumberToVerify = ""    // Clear flag
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                postEvent(EventType.ShowVerificationCodeDialog)
            }
        }

    /**
     * Link current user with phone number (if [code] matches the [storedVerificationId]).
     */
    fun handlePhoneVerificationCode(code: String) =
        Authentication.linkWithPhone(storedVerificationId, code)?.withProgressBar()
            ?.addOnCompleteListener { phoneNumberToVerify = ""    /* Clear flag */ }

    /**
     * Link current user with Facebook account with given [token].
     */
    fun handleFacebookAccessToken(token: AccessToken) =
        Authentication.linkWithFacebook(token)?.withProgressBar()

    /**
     * Link current user with Google account (token in [intent]).
     */
    fun handleGoogleResult(intent: Intent?) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        try {
            // Google Sign In was successful, authenticate with Firebase
            task.getResult(ApiException::class.java)?.let { account ->
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                account.idToken?.let { Authentication.linkWithGoogle(it)?.withProgressBar() }
            }
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "Google sign in failed", e)
        }
    }

    /**
     * Unlink current user with the given [providerNameResId].
     */
    fun unlinkProvider(providerNameResId: Int) =
        Authentication.unlinkProvider(providerNameResId)?.withProgressBar()
}