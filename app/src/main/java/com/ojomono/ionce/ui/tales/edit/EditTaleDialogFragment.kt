package com.ojomono.ionce.ui.tales.edit

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
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
import com.ojomono.ionce.utils.StringResource

/**
 * A [DialogFragment] representing the edit screen for a tale.
 * Use the [EditTaleDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditTaleDialogFragment : DialogFragment() {

    private val layoutId = R.layout.fragment_edit_tale_dialog
    lateinit var binding: FragmentEditTaleDialogBinding
    lateinit var viewModel: EditTaleViewModel

    private var taleId: String? = null

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { taleId = it.getString(ARG_TALE_ID) }
    }

    /** The system calls this to get the DialogFragment's layout, regardless
    of whether it's being displayed as a dialog or an embedded fragment. */
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

        // Set the action bar
        setActionBar(binding.toolbar)

        return binding.root
    }

    /** The system calls this only when creating the layout in a dialog. */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
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

    override fun dismiss() {
        // Hide on-screen soft keyboard (if shown) before dismissing the dialog.
        (context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(view?.windowToken, 0)

        // Dismiss dialog
        super.dismiss()
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
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel)
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
        if (!viewModel.taleChanged()) dismiss()

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
}