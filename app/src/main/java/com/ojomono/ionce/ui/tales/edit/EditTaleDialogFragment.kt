package com.ojomono.ionce.ui.tales.edit

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentEditTaleDialogBinding
import com.ojomono.ionce.ui.dialogs.AlertDialog
import com.ojomono.ionce.ui.bases.BaseDialogFragment
import com.ojomono.ionce.ui.bases.BaseViewModel
import com.ojomono.ionce.utils.StringResource
import com.ojomono.ionce.utils.proxies.ImageCompressor

/**
 * A [DialogFragment] representing the edit screen for a tale.
 * Use the [EditTaleDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditTaleDialogFragment : BaseDialogFragment() {

    /**********************/
    /** Companion object **/
    /**********************/

    companion object {

        // Fragment tags
        const val FT_DISCARD = "discard"

        // the fragment initialization parameters
        private const val ARG_TALE_ID = "tale-id"

        /**
         * Use this factory method to create a new instance of
         * this fragment for editing the tale with the given [taleId]. Empty means a new tale.
         */
        @JvmStatic
        fun newInstance(taleId: String) =
            EditTaleDialogFragment().apply {
                arguments = Bundle().apply { putString(ARG_TALE_ID, taleId) }
            }
    }

    /************/
    /** Fields **/
    /************/

    // Base fields
    override val layoutId = R.layout.fragment_edit_tale_dialog
    override lateinit var binding: FragmentEditTaleDialogBinding
    override lateinit var viewModel: EditTaleViewModel
    override val progressBar: ProgressBar? = null

    private var taleId: String? = null

    // Activity result launchers
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) onImagePicked(it)
        }

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the arguments
        arguments?.let { taleId = it.getString(ARG_TALE_ID) }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Override dialog's onBackPressed() to allow confirmation pop up before dismissing
        return object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                discardAndDismiss()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Init the viewModel using it's factory. If no taleId is found, use empty.
        val viewModelFactory = EditTaleModelFactory(taleId ?: "")
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(EditTaleViewModel::class.java)
        binding = getDataBinding(inflater, container)
        binding.viewModel = viewModel
        observeEvents()

        // Set the action bar
        setActionBar(binding.toolbar)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        activity?.menuInflater?.inflate(R.menu.edit_tale_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                // handle confirmation button click here
                saveAndDismiss()
                true
            }
            android.R.id.home -> {
                // handle close button click here
                discardAndDismiss()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /********************************************/
    /** EventStateHolder.EventObserver methods **/
    /********************************************/

    override fun handleEvent(event: BaseViewModel.BaseEventType) {
        super.handleEvent(event)
        when (event) {
            is EditTaleViewModel.EventType.ShowImagePicker -> showImagePicker()
        }
    }

    /*********************/
    /** private methods **/
    /*********************/

    /**
     * Set the action bar with the given [toolbar].
     */
    private fun setActionBar(toolbar: Toolbar) {
        // Set different title for new or updated tale
        val title =
            StringResource(
                if (taleId.isNullOrEmpty()) R.string.edit_tale_screen_title_new
                else R.string.edit_tale_screen_title_update
            )

        // Call super method
        super.setActionBar(toolbar, title)
    }

    /**
     * Save tale to database (if data is valid) and dismiss the dialog.
     */
    private fun saveAndDismiss() {

        // Title is required, so if it's empty show error
        if (binding.etTitle.text.isNullOrEmpty())
            binding.etTitle.error = getString(R.string.edit_tale_title_error)

        // Else, save tale and dismiss dialog
        else {
            viewModel.saveTale()
            dismiss()
        }
    }

    /**
     * Confirm discard (if any changes were made) and dismiss the dialog.
     */
    private fun discardAndDismiss() {

        // If no change was made, dismiss dialog
        if (!viewModel.didTaleChange()) dismiss()

        // Else, ask user to confirm changes discard
        else {
            AlertDialog(
                message = StringResource(
                    if (taleId.isNullOrEmpty()) R.string.edit_tale_discard_dialog_message_new
                    else R.string.edit_tale_discard_dialog_message_update
                ),
                onPositive = ::dismiss,
                okButtonText = StringResource(R.string.edit_tale_discard_dialog_positive_button_text)
            ).show(parentFragmentManager, FT_DISCARD)
        }
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
//        ImageLoader.load(context, ImageLoader.UPLOADING_IN_PROGRESS, binding.imageCover)

        // Compress image and set it as user photo
        ImageCompressor.compress(context, uri) {
            activity?.runOnUiThread { viewModel.updateDisplayedCover(it) }
        }
    }

}