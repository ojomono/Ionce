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
import com.ojomono.ionce.firebase.Authentication.handleCollision
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
        object DismissCurrentDialog : EventType()
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
                determineEventForProviderClick(
                    emailUserInfo,
                    EventType.ShowEmailAddressDialog,
                    R.string.profile_email_provider_name
                )
            R.id.linear_phone_provider ->
                determineEventForProviderClick(
                    phoneUserInfo,
                    EventType.ShowPhoneNumberDialog,
                    R.string.profile_phone_provider_name
                )
            R.id.linear_twitter_provider ->
                determineEventForProviderClick(
                    twitterUserInfo,
                    EventType.ShowLinkWithTwitter,
                    R.string.profile_twitter_provider_name
                )
            R.id.linear_facebook_provider ->
                determineEventForProviderClick(
                    facebookUserInfo,
                    EventType.ShowLinkWithFacebook,
                    R.string.profile_facebook_provider_name
                )
            R.id.linear_google_provider ->
                determineEventForProviderClick(
                    googleUserInfo,
                    EventType.ShowLinkWithGoogle,
                    R.string.profile_google_provider_name
                )
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
            handleFacebookAccessToken(loginResult.accessToken)
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

                // If verified automatically, no need for the manual dialog
                postEvent(EventType.DismissCurrentDialog)
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
                // If phone number is empty maybe the automatic verification already completed
                // and anyway there is nothing to compare to...
                if (phoneNumberToVerify.isNotEmpty())
                    postEvent(EventType.ShowVerificationCodeDialog)
            }
        }

    /**
     * Link current user with phone number (if [code] matches the [storedVerificationId]).
     */
    fun handlePhoneVerificationCode(code: String) =
        Authentication.linkWithPhone(storedVerificationId, code)
            ?.handleCollision(::showErrorMessage)?.withProgressBar()
            ?.addOnCompleteListener { phoneNumberToVerify = ""    /* Clear flag */ }

    /**
     * Link current user with Facebook account with given [token].
     */
    fun handleFacebookAccessToken(token: AccessToken) =
        Authentication.linkWithFacebook(token)
            ?.handleCollision(::showErrorMessage)?.withProgressBar()

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
                account.idToken?.let {
                    Authentication.linkWithGoogle(it)
                        ?.handleCollision(::showErrorMessage)?.withProgressBar()
                }
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


    /*********************/
    /** private methods **/
    /*********************/

    /**
     * Determine if the [providerData] needs to be linked (using the given [linkEvent]) or unlinked
     * (using the common unlink event with [nameResId]), and check if possible.
     */
    private fun determineEventForProviderClick(
        providerData: LiveData<UserInfo>,
        linkEvent: EventType,
        nameResId: Int
    ) =
        user.value?.providerData?.size?.let {

            when {
                // If the provider is not linked yet, return it's "link event"
                providerData.value == null -> linkEvent

                // If it's linked but it's the only provider (aside from the default "firebase") -
                // return an error event
                it <= MIN_NUMBER_OF_PROVIDERS ->
                    ShowErrorMessageByResId(R.string.profile_error_last_provider)

                // If it's linked and is not the only provider - return the "unlink event"
                else -> EventType.ShowUnlinkProviderDialog(nameResId)
            }
        }

    /***************/
    /** Constants **/
    /***************/

    companion object {
        // Used for checking if unlinking should be allowed
        const val MIN_NUMBER_OF_PROVIDERS = 2   // Default ("firebase") + 1 (real provider)
    }
}