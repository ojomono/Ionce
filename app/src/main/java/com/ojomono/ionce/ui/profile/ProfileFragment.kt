package com.ojomono.ionce.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.auth.FirebaseAuthUserCollisionException  // TODO avoid importing firebase packages here
import com.google.firebase.auth.OAuthProvider       // TODO avoid importing firebase packages here
import com.google.firebase.auth.TwitterAuthProvider // TODO avoid importing firebase packages here
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentProfileBinding
import com.ojomono.ionce.utils.*
import kotlinx.android.synthetic.main.fragment_profile.*


class ProfileFragment : BaseFragment() {

    /************/
    /** Fields **/
    /************/

    override val layoutId = R.layout.fragment_profile
    override lateinit var binding: FragmentProfileBinding
    override lateinit var viewModel: ProfileViewModel

    private lateinit var facebookCallbackManager: CallbackManager

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

        // Initialize Facebook Login button
        initFacebookLoginButton()

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { viewModel.updateUserPicture(it)?.withProgressBar(progress_bar) }
        }

        // Pass the activity result back to the Facebook SDK
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    /**************************/
    /** BaseFragment methods **/
    /**************************/

    override fun handleEvent(event: BaseViewModel.Event) {
        when (event) {
            is ProfileViewModel.EventType.ShowPopupMenu -> showPopupMenu(event.view)
            is ProfileViewModel.EventType.ShowImagePicker -> showImagePicker()
            is ProfileViewModel.EventType.ShowEditNameDialog -> showEditNameDialog()
            is ProfileViewModel.EventType.ShowLinkWithTwitter -> showLinkWithTwitter()
        }
    }

    /***********************/
    /** private methods **/
    /***********************/

    /**
     * Initialize the Facebook login button callback.
     */
    private fun initFacebookLoginButton() {
        facebookCallbackManager = CallbackManager.Factory.create()

        binding.buttonFacebookLogin.setPermissions(FP_EMAIL, FP_PUBLIC_PROFILE)
        binding.buttonFacebookLogin.fragment = this
        binding.buttonFacebookLogin.registerCallback(
            facebookCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(TAG, "facebook:onSuccess:$loginResult")
                    viewModel.handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Log.d(TAG, "facebook:onCancel")
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG, "facebook:onError", error)
                }
            })

        // Redirect clicks on the Facebook provider linear layout to the login button
        binding.linearFacebookProvider.setOnClickListener {
            binding.buttonFacebookLogin.callOnClick()
        }
    }

    /**
     * Show the settings menu as a popup menu attached to [v].
     */
    private fun showPopupMenu(v: View) {
        PopupMenu(context, v).apply {
            // ProfileViewModel implements OnMenuItemClickListener
            setOnMenuItemClickListener(viewModel)
            inflate(R.menu.profile_settings_menu)
            show()
        }
    }

    /**
     * Show the image picker.
     */
    private fun showImagePicker() {
        val intent =
            Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI
            )
        startActivityForResult(intent, RC_PICK_IMAGE)
    }

    /**
     * Show dialog for updating the current user's name.
     */
    private fun showEditNameDialog() =
        AlertDialog.Builder(context)
            .setTitle(R.string.profile_edit_name_dialog_title)
            .setInputAndSaveButton(
                viewModel::updateUserName,
                progress_bar,
                viewModel.user.value?.displayName ?: ""
            ).setCancelButton()
            .create()
            .show()

    /**
     * Show activity for linking with Twitter.
     */
    private fun showLinkWithTwitter() {
        val provider = OAuthProvider.newBuilder(TwitterAuthProvider.PROVIDER_ID)

        activity?.let {
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
    }

    /***************/
    /** Constants **/
    /***************/

    companion object {
        // Request codes
        const val RC_PICK_IMAGE = 1

        // Facebook login button permissions
        const val FP_EMAIL = "email"
        const val FP_PUBLIC_PROFILE = "public_profile"
    }
}