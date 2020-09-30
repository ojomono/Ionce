package com.ojomono.ionce.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.Task
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentProfileBinding
import com.ojomono.ionce.SplashActivity
import com.ojomono.ionce.utils.withProgressBar
import kotlinx.android.synthetic.main.fragment_profile.*


class ProfileFragment : Fragment() {

    /************/
    /** Fields **/
    /************/

    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel

    // Function to run when a new image was picked
    private lateinit var onImagePicked: (Uri) -> Task<Void>?     // TODO: maybe get rid of member

    /**********************/
    /** Fragment methods **/
    /**********************/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_profile,
            container,
            false
        )

        // Set the viewmodel for databinding - this allows the bound layout access
        // to all the model in the VieWModel
        binding.profileViewModel = viewModel

        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = viewLifecycleOwner

        // Observe if an event was thrown
        viewModel.event.observe(
            viewLifecycleOwner, {
                it.consume { event ->
                    when (event) {
                        is ProfileViewModel.EventType.EditEmailEvent ->
                            showEditEmailDialog(event.func)
                        is ProfileViewModel.EventType.ChangePhotoEvent ->
                            showImagePicker(event.func)
                        is ProfileViewModel.EventType.EditNameEvent ->
                            showEditNameDialog(event.func)
                        is ProfileViewModel.EventType.SignOutEvent ->
                            executeSignOut(event.func)
                    }
                }
            }
        )

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { onImagePicked(it)?.withProgressBar(progress_bar) }
        }
    }

    /***********************/
    /** private methods **/
    /***********************/

    /**
     * Sign out the user using the given [func] and go back to [SplashActivity].
     */
    private fun executeSignOut(func: (Context) -> Task<Void>?) {
        context?.let { context ->
            func(context)?.withProgressBar(progress_bar)
                ?.addOnCompleteListener {
                    // User is now signed out - go back to splash screen
                    startActivity(Intent(context, SplashActivity::class.java))
                    activity?.finish()
                }
        }
    }

    /**
     * Show the image picker. Picked image will be set to [func] function.
     */
    private fun showImagePicker(func: (Uri) -> Task<Void>?) {
        onImagePicked = func
        val intent =
            Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI
            )
        startActivityForResult(intent, RC_PICK_IMAGE)
    }

    /**
     * Build a dialog builder for updating the current user's name, using [func] as the listener
     * function of the positive button.
     */
    private fun showEditNameDialog(func: (String) -> Task<Void>?) {
        val dialogBuilder = AlertDialog.Builder(context)

        val input = EditText(context)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        input.setText(viewModel.user.value?.displayName)

        dialogBuilder.setView(input)

        dialogBuilder.setTitle(getText(R.string.profile_edit_name_dialog_title))
        dialogBuilder
            .setPositiveButton(getText(R.string.dialogs_positive_button_text))
            { dialog, _ ->
                func(input.text.toString())?.withProgressBar(progress_bar)
                dialog.cancel()
            }

        // Add the 'cancel' button
        dialogBuilder
            .setNegativeButton(getText(R.string.dialogs_negative_button_text))
            { dialog, _ -> dialog.cancel() }

        // Create the dialog and show it
        dialogBuilder.create().show()
    }

    // TODO make a generic dialog builder (changing input-text, title)

    /**
     * Build a dialog builder for updating the current user's email, using [func] as the listener
     * function of the positive button.
     */
    private fun showEditEmailDialog(func: (String) -> Task<Void>?) {
        val dialogBuilder = AlertDialog.Builder(context)

        val input = EditText(context)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        input.setText(viewModel.user.value?.email)

        dialogBuilder.setView(input)

        dialogBuilder.setTitle(getText(R.string.profile_edit_email_dialog_title))
        dialogBuilder
            .setPositiveButton(getText(R.string.dialogs_positive_button_text))
            { dialog, _ ->
                func(input.text.toString())?.withProgressBar(progress_bar)
                dialog.cancel()
            }

        // Add the 'cancel' button
        dialogBuilder
            .setNegativeButton(getText(R.string.dialogs_negative_button_text))
            { dialog, _ -> dialog.cancel() }

        // Create the dialog and show it
        dialogBuilder.create().show()
    }

    /***************/
    /** Constants **/
    /***************/

    companion object {
        // Request codes
        const val RC_PICK_IMAGE = 1
    }
}