package com.ojomono.ionce.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
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

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Init the ViewModel
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        // Init the DataBinding
        initDataBinding(inflater, container)

        // Observe possible events
        observeEvents()

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { viewModel.updateUserPicture(it)?.withProgressBar(progress_bar) }
        }
    }

    /**************************/
    /** BaseFragment methods **/
    /**************************/

    override fun handleEvent(event: BaseViewModel.Event) {
        when (event) {
            is ProfileViewModel.EventType.ShowPopupMenu -> showPopupMenu(event.view)
            is ProfileViewModel.EventType.ShowImagePicker -> showImagePicker()
            is ProfileViewModel.EventType.ShowEditNameDialog -> showEditNameDialog()
        }
    }

    /***********************/
    /** private methods **/
    /***********************/

    /**
     * Init the [binding] field.
     */
    private fun initDataBinding(inflater: LayoutInflater, container: ViewGroup?) {
        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        // Set the viewmodel for databinding - this allows the bound layout access
        // to all the model in the VieWModel
        binding.viewModel = viewModel

        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = viewLifecycleOwner
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

    /***************/
    /** Constants **/
    /***************/

    companion object {
        // Request codes
        const val RC_PICK_IMAGE = 1
    }
}