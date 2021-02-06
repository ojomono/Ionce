package com.ojomono.ionce.ui.tales.edit

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentEditTaleDialogBinding
import com.ojomono.ionce.ui.dialogs.AlertDialogFragment
import com.ojomono.ionce.utils.EventStateHolder
import com.ojomono.ionce.utils.ImageUtils
import com.ojomono.ionce.utils.StringResource

/**
 * A [DialogFragment] representing the edit screen for a tale.
 * Use the [EditTaleDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditTaleDialogFragment : DialogFragment(),
    EventStateHolder.EventObserver<EditTaleViewModel.EventType> {

    private val layoutId = R.layout.fragment_edit_tale_dialog
    lateinit var binding: FragmentEditTaleDialogBinding
    lateinit var viewModel: EditTaleViewModel

    private var taleId: String? = null

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the arguments
        arguments?.let { taleId = it.getString(ARG_TALE_ID) }

        // Set style to be app theme (instead of default dialog theme) to make full-screen dialog
        // (actually the important thing is: <item name="android:windowIsFloating">false</item>)
        setStyle(STYLE_NORMAL, R.style.AppTheme)
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

        // Inflate view and obtain an instance of the binding class (like in BaseFragment)
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        // Observe screen events
        viewModel.events.observeEvents(viewLifecycleOwner, this)

        // Set the action bar
        setActionBar(binding.toolbar)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Set the enter & exit sliding animations
        dialog?.window?.setWindowAnimations(R.style.AppTheme_FullScreenDialog)
    }

    override fun onPause() {
        super.onPause()

        // Disable enter animation for this instance to avoid animation on back from image picker
        dialog?.window?.setWindowAnimations(R.style.AppTheme_FullScreenDialog_NoEnterAnim)
    }

    override fun dismiss() {
        // Hide on-screen soft keyboard (if shown) before dismissing the dialog.
        (context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(view?.windowToken, 0)

        // Dismiss dialog
        super.dismiss()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_PICK_IMAGE ->
                if (resultCode == Activity.RESULT_OK) data?.data?.let { onImagePicked(it) }
        }
    }

    /********************************************/
    /** EventStateHolder.EventObserver methods **/
    /********************************************/

    override fun handleEvent(event: EditTaleViewModel.EventType) {
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
        toolbar.title =
            getString(
                if (taleId.isNullOrEmpty()) R.string.edit_tale_screen_title_new
                else R.string.edit_tale_screen_title_update
            )

        (activity as AppCompatActivity?)?.setSupportActionBar(toolbar)

        val actionBar: ActionBar? = (activity as AppCompatActivity?)?.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_close_24)
        }
        setHasOptionsMenu(true)
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
            AlertDialogFragment(
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
    private fun showImagePicker() =
        startActivityForResult(
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI),
            RC_PICK_IMAGE
        )

    /**
     * Compress image from [uri] and set as user photo.
     */
    private fun onImagePicked(uri: Uri) {

        // Put a progress bar in the image view
//        ImageUtils.load(context, ImageUtils.UPLOADING_IN_PROGRESS, binding.imageCover)

        // Compress image and set it as user photo
        ImageUtils.compress(context, uri) {
            activity?.runOnUiThread { viewModel.updateDisplayedCover(it) }
        }
    }

    /**********************/
    /** Companion object **/
    /**********************/

    companion object {

        // Request codes
        const val RC_PICK_IMAGE = 1

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
}