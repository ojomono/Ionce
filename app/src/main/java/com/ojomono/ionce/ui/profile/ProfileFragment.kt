package com.ojomono.ionce.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseException    // TODO avoid importing firebase packages here
import com.google.firebase.auth.*   // TODO avoid importing firebase packages here
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentProfileBinding
import com.ojomono.ionce.ui.dialogs.InputDialog
import com.ojomono.ionce.ui.dialogs.AlertDialog
import com.ojomono.ionce.utils.*
import com.ojomono.ionce.utils.bases.BaseFragment
import com.ojomono.ionce.utils.bases.BaseViewModel
import com.ojomono.ionce.utils.images.ImageCompressor
import com.ojomono.ionce.utils.images.ImageLoader
import java.io.*
import java.util.concurrent.TimeUnit

class ProfileFragment : BaseFragment() {

    /************/
    /** Fields **/
    /************/

    // Base fields
    override val layoutId = R.layout.fragment_profile
    override lateinit var binding: FragmentProfileBinding
    override lateinit var viewModel: ProfileViewModel
    override lateinit var progressBar: ProgressBar

    // Activity result launchers
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) onImagePicked(it)
        }
    private val linkWithGoogle =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) viewModel.handleGoogleResult(it.data)
        }

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        binding = getDataBinding(inflater, container)
        binding.viewModel = viewModel
        progressBar = binding.progressBar
        observeEvents()

        // If we are in the middle of a phone number verification - continue it
        viewModel.phoneNumberToVerify.let { if (it.isNotEmpty()) verifyPhoneNumber(it) }

        // Initialize providers requirements
        initFacebookLoginButton()
        initGoogleSignInClient()

        return binding.root
    }

    // TODO use registerForActivityResult when Facebook API will update
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Pass the activity result back to the Facebook SDK
        viewModel.facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    /**************************/
    /** BaseFragment methods **/
    /**************************/

    override fun handleEvent(event: BaseViewModel.BaseEventType) {
        super.handleEvent(event)
        when (event) {
            is ProfileViewModel.EventType.ShowPopupMenu -> showPopupMenu(event.view)
            is ProfileViewModel.EventType.ShowImagePicker -> showImagePicker()
            is ProfileViewModel.EventType.ShowNameEditDialog -> showNameEditDialog()
            is ProfileViewModel.EventType.ShowEmailAddressDialog -> showEmailAddressDialog()
            is ProfileViewModel.EventType.ShowPhoneNumberDialog -> showPhoneVerifyDialog()
            is ProfileViewModel.EventType.ShowLinkWithTwitter -> showLinkWithTwitter()
            is ProfileViewModel.EventType.ShowLinkWithFacebook -> showLinkWithFacebook()
            is ProfileViewModel.EventType.ShowLinkWithGoogle -> showLinkWithGoogle()
            is ProfileViewModel.EventType.ShowUnlinkProviderDialog ->
                showUnlinkProviderDialog(event.providerNameResId)
        }
    }

    /***********************/
    /** private methods **/
    /***********************/

    /**
     * Initialize the Facebook login button callback.
     */
    private fun initFacebookLoginButton() =
        binding.buttonFacebookLogin.apply {
            setPermissions(FP_EMAIL, FP_PUBLIC_PROFILE)
            fragment = this@ProfileFragment
            registerCallback(viewModel.facebookCallbackManager, viewModel.getFacebookCallback())
        }

    /**
     * Initialize the Google sign in client.
     */
    private fun initGoogleSignInClient() =
        activity?.let {

            // Configure Google Sign In
            val gso =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

            // Build a GoogleSignInClient with the options specified by gso.
            viewModel.googleSignInClient = GoogleSignIn.getClient(it, gso)
        }

    /**
     * Show the settings menu as a popup menu attached to [v].
     */
    private fun showPopupMenu(v: View) =
        PopupMenu(context, v).apply {
            // ProfileViewModel implements OnMenuItemClickListener
            setOnMenuItemClickListener(viewModel)
            inflate(R.menu.profile_settings_menu)
            show()
        }

    /**
     * Show the image picker.
     */
    private fun showImagePicker() = pickImage.launch("image/*")

    /**
     * Compress image from [uri] and set as user photo.
     */
    private fun onImagePicked(uri: Uri) {

        // Put a progress bar in the image view
//        ImageLoader.load(context, ImageLoader.UPLOADING_IN_PROGRESS, binding.imageProfilePicture)

        // Compress image and set it as user photo
        ImageCompressor.compress(context, uri) {
            // TODO run in background when refactoring to full use of coroutines
            activity?.runOnUiThread { viewModel.updateUserPicture(it) }
        }
    }

    /**
     * Show dialog for updating the current user's name.
     */
    private fun showNameEditDialog() =
        InputDialog(
            title = StringResource(R.string.profile_name_edit_dialog_title),
            onPositive = viewModel::updateUserName,
            defaultInputText = StringResource(viewModel.user.value?.displayName ?: "")
        ).show(parentFragmentManager, FT_NAME)

    /**
     * Present the user an interface that prompts them to type their email address.
     */
    private fun showEmailAddressDialog() =
        InputDialog(
            title = StringResource(R.string.profile_email_link_dialog_title),
            onPositive = viewModel::sendSignInLinkToEmail,
            okButtonText = StringResource(R.string.profile_email_link_dialog_button),
            defaultInputText = StringResource(viewModel.user.value?.email ?: "")
        ).show(parentFragmentManager, FT_EMAIL)

    /**
     * Present the user an interface that prompts them to type their phone number.
     */
    private fun showPhoneVerifyDialog() =
        InputDialog(
            title = StringResource(R.string.profile_phone_verify_dialog_title),
            message = StringResource(R.string.profile_phone_verify_dialog_message),
            onPositive = ::verifyPhoneNumber,
            okButtonText = StringResource(R.string.profile_phone_verify_dialog_button),
            defaultInputText = StringResource(viewModel.user.value?.phoneNumber ?: "")
        ).show(parentFragmentManager, FT_PHONE_NUMBER)

    /**
     * Verify the given [phoneNumber].
     */
    private fun verifyPhoneNumber(phoneNumber: String) =
        // The phone number should be in a format that can be parsed into E.164 format
        if (PhoneNumberUtils.isWellFormedSmsAddress(phoneNumber))
            activity?.let {
                val options = PhoneAuthOptions.newBuilder()
                    .setPhoneNumber(phoneNumber)       // Phone number to verify
                    .setTimeout(PHONE_VERIFICATION_TIMEOUT, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(it)                 // Activity (for callback binding)
                    .setCallbacks(getPhoneVerificationCallbacks())    // OnVerificationStateChangedCallbacks
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)

                // Show progress bar until verification complete (manually as we don't have a task)
                // This is hidden in each callback option from: getPhoneVerificationCallbacks()
                progressBar.visibility = View.VISIBLE

                // Save phone number for instance state changes
                viewModel.phoneNumberToVerify = phoneNumber
            }
        else Toast.makeText(
            context,
            R.string.profile_phone_verify_invalid_credential_message,
            Toast.LENGTH_SHORT
        ).show()

    // TODO Move getPhoneVerificationCallbacks to ProfileViewModel. in onVerificationCompleted,
    //  there is a need to raise two different events in the same function (withProgressBar &
    //  dismissCurrentDialog) which can result in overriding. In order to move it, a separate
    //  event-holder-LiveData should be set in BaseViewModel: Separating the progress bar events
    //  from the rest. Also notice the manual hiding of the progress bar in all callbacks.

    /**
     * Get the callback object for the phone verification process.
     */
    private fun getPhoneVerificationCallbacks() =

        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Hide progress bar showed in: verifyPhoneNumber fun
                progressBar.visibility = View.GONE

                Log.d(TAG, "onVerificationCompleted:$credential")
                viewModel.handlePhoneVerificationComplete(credential).withProgressBar(progressBar)
                viewModel.phoneNumberToVerify = ""    // Clear flag

                // If verified automatically, no need for the manual dialog
                (parentFragmentManager.findFragmentByTag(FT_PHONE_CODE) as
                        InputDialog<*>).dismiss()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // Hide progress bar showed in: verifyPhoneNumber fun
                progressBar.visibility = View.GONE

                Log.w(TAG, "onVerificationFailed", e)
                Toast.makeText(
                    context,
                    viewModel.getPhoneVerificationFailedMessage(e),
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.phoneNumberToVerify = ""    // Clear flag
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // Hide progress bar showed in: verifyPhoneNumber fun
                progressBar.visibility = View.GONE

                Log.d(TAG, "onCodeSent:$verificationId")
                viewModel.storedVerificationId = verificationId
                // If phone number is EMPTY maybe the automatic verification already completed
                // and anyway there is nothing to compare to...
                if (viewModel.phoneNumberToVerify.isNotEmpty()) showVerificationCodeDialog()
            }
        }

    /**
     * Present the user an interface that prompts them to type the verification code from the SMS
     * message.
     */
    private fun showVerificationCodeDialog() =
        InputDialog(
            title = StringResource(R.string.profile_phone_verify_dialog_title),
            message = StringResource(
                getString(
                    R.string.profile_phone_verification_code_dialog_message,
                    viewModel.phoneNumberToVerify
                )
            ),
            onPositive = viewModel::handlePhoneVerificationCode,
            okButtonText = StringResource(getString(R.string.profile_phone_verify_dialog_button))
        ).show(parentFragmentManager, FT_PHONE_CODE)

    /**
     * Show activity for linking with Twitter.
     */
    private fun showLinkWithTwitter() =
        activity?.let {
            val provider =
                OAuthProvider.newBuilder(TwitterAuthProvider.PROVIDER_ID)

            viewModel.user.value
                ?.startActivityForLinkWithProvider(it, provider.build())
                ?.withProgressBar(binding.progressBar)
                ?.addOnSuccessListener { viewModel.refresh() }
                ?.addOnFailureListener { e ->
                    // Handle failure.
                    if (e is FirebaseAuthUserCollisionException)
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    // TODO merge accounts in case of collision.
                }
        }

    /**
     * Show activity for linking with Facebook.
     */
    private fun showLinkWithFacebook() = binding.buttonFacebookLogin.callOnClick()

    /**
     * Show activity for linking with Google.
     */
    private fun showLinkWithGoogle() =
        linkWithGoogle.launch(viewModel.googleSignInClient.signInIntent)

    /**
     * Show dialog for verifying decision to unlink given [providerNameResId].
     */
    private fun showUnlinkProviderDialog(providerNameResId: Int) =
        AlertDialog(
            message = StringResource(
                getString(
                    R.string.profile_unlink_provider_dialog_message,
                    getString(providerNameResId)
                )
            ),
            onPositive = { viewModel.unlinkProvider(providerNameResId) },
            okButtonText = StringResource(getString(R.string.profile_unlink_provider_positive_button_text))
        ).show(parentFragmentManager, FT_UNLINK)

    /***************/
    /** Constants **/
    /***************/

    companion object {
        // Facebook login button permissions
        const val FP_EMAIL = "email"
        const val FP_PUBLIC_PROFILE = "public_profile"

        // Fragment tags
        const val FT_NAME = "name"
        const val FT_EMAIL = "email"
        const val FT_PHONE_NUMBER = "phone_number"
        const val FT_PHONE_CODE = "phone_code"
        const val FT_UNLINK = "unlink"

        // others
        const val PHONE_VERIFICATION_TIMEOUT = 60L
    }
}