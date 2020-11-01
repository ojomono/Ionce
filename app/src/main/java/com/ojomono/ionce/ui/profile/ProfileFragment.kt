package com.ojomono.ionce.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*   // TODO avoid importing firebase packages here
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentProfileBinding
import com.ojomono.ionce.utils.*
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.concurrent.TimeUnit

class ProfileFragment : BaseFragment() {

    /************/
    /** Fields **/
    /************/

    // Base fields
    override val layoutId = R.layout.fragment_profile
    override lateinit var binding: FragmentProfileBinding
    override lateinit var viewModel: ProfileViewModel

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        binding = getDataBinding(inflater, container)
        binding.viewModel = viewModel
        observeEvents()

        // If we are in the middle of a phone number verification - continue it
        viewModel.phoneNumberToVerify.let { if (it.isNotEmpty()) verifyPhoneNumber(it) }

        // Initialize providers requirements
        initFacebookLoginButton()
        initGoogleSignInClient()

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_PICK_IMAGE -> if (resultCode == Activity.RESULT_OK) onImagePicked(data?.data)
            RC_LINK_GOOGLE -> viewModel.handleGoogleResult(data)
        }

        // Pass the activity result back to the Facebook SDK
        viewModel.facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    /**************************/
    /** BaseFragment methods **/
    /**************************/

    override fun handleEvent(event: BaseViewModel.Event) {
        when (event) {
            is ProfileViewModel.EventType.ShowPopupMenu -> showPopupMenu(event.view)
            is ProfileViewModel.EventType.ShowImagePicker -> showImagePicker()
            is ProfileViewModel.EventType.ShowEditNameDialog -> showEditNameDialog()
            is ProfileViewModel.EventType.ShowTypePhoneDialog -> showPhoneVerifyDialog()
            is ProfileViewModel.EventType.ShowLinkWithTwitter -> showLinkWithTwitter()
            is ProfileViewModel.EventType.ShowLinkWithGoogle -> showLinkWithGoogle()
            is ProfileViewModel.EventType.ShowProgressBar ->
                event.task.withProgressBar(progress_bar)
            is ProfileViewModel.EventType.ShowErrorMessage ->
                Toast.makeText(context, event.messageResId, Toast.LENGTH_SHORT).show()
        }
    }

    /***********************/
    /** private methods **/
    /***********************/

    /**
     * Initialize the Facebook login button callback.
     */
    private fun initFacebookLoginButton() = binding.buttonFacebookLogin.apply {
        setPermissions(FP_EMAIL, FP_PUBLIC_PROFILE)
        fragment = this@ProfileFragment
        registerCallback(viewModel.facebookCallbackManager, viewModel.getFacebookCallback())

        // Redirect clicks on the Facebook provider linear layout to the login button
        binding.linearFacebookProvider.setOnClickListener { callOnClick() }
    }

    /**
     * Initialize the Google sign in client.
     */
    private fun initGoogleSignInClient() = activity?.let {

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
    private fun showPopupMenu(v: View) = PopupMenu(context, v).apply {
        // ProfileViewModel implements OnMenuItemClickListener
        setOnMenuItemClickListener(viewModel)
        inflate(R.menu.profile_settings_menu)
        show()
    }

    /**
     * Show the image picker.
     */
    private fun showImagePicker() =
        startActivityForResult(viewModel.getImagePickerIntent(), RC_PICK_IMAGE)

    /**
     * Update user's picture to given [uri] image.
     */
    private fun onImagePicked(uri: Uri?) =
        uri?.let { viewModel.updateUserPicture(it)?.withProgressBar(progress_bar) }

    /**
     * Show dialog for updating the current user's name.
     */
    private fun showEditNameDialog() =
        AlertDialog.Builder(context)
            .setTitle(R.string.profile_name_edit_dialog_title)
            .setInputAndCustomButton(
                viewModel::updateUserName,
                progress_bar,
                viewModel.user.value?.displayName ?: ""
            ).setCancelButton()
            .create()
            .show()

    /**
     * Present the user an interface that prompts them to type their phone number.
     */
    private fun showPhoneVerifyDialog() =
        AlertDialog.Builder(context)
            .setTitle(R.string.profile_phone_verify_dialog_title)
            .setMessage(R.string.profile_phone_verify_dialog_message)
            .setInputAndCustomButton(
                ::verifyPhoneNumber,
                R.string.profile_phone_verify_dialog_button
            )
            .setCancelButton()
            .create()
            .show()

    /**
     * Verify the given [phoneNumber].
     */
    private fun verifyPhoneNumber(phoneNumber: String) = activity?.let {
        val options = PhoneAuthOptions.newBuilder()
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(PHONE_VERIFICATION_TIMEOUT, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(it)                 // Activity (for callback binding)
            .setCallbacks(viewModel.getPhoneVerificationCallbacks())    // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        // Save phone number for instance state changes
        viewModel.phoneNumberToVerify = phoneNumber
    }

    /**
     * Show activity for linking with Twitter.
     */
    private fun showLinkWithTwitter() = activity?.let {
        val provider = OAuthProvider.newBuilder(TwitterAuthProvider.PROVIDER_ID)

        viewModel.user.value
            ?.startActivityForLinkWithProvider(it, provider.build())
            ?.withProgressBar(progress_bar)
            ?.addOnSuccessListener { viewModel.refresh() }
            ?.addOnFailureListener { e ->
                // Handle failure.
                if (e is FirebaseAuthUserCollisionException)
                // TODO merge users in case of collision.
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Show activity for linking with Google.
     */
    private fun showLinkWithGoogle() =
        startActivityForResult(viewModel.googleSignInClient.signInIntent, RC_LINK_GOOGLE)

    /***************/
    /** Constants **/
    /***************/

    companion object {
        // Request codes
        const val RC_PICK_IMAGE = 1
        const val RC_LINK_GOOGLE = 2

        // Facebook login button permissions
        const val FP_EMAIL = "email"
        const val FP_PUBLIC_PROFILE = "public_profile"

        // others
        const val PHONE_VERIFICATION_TIMEOUT = 60L
    }
}