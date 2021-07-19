package com.ojomono.ionce.ui.roll.group

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentGroupRollDialogBinding
import com.ojomono.ionce.ui.dialogs.AlertDialog
import com.ojomono.ionce.utils.bases.BaseDialogFragment
import com.ojomono.ionce.utils.StringResource
import com.ojomono.ionce.utils.bases.BaseViewModel
import com.ojomono.ionce.utils.proxies.QRCodeScanner

/**
 * A [DialogFragment] representing the management screen for a roll group.
 * Use the [GroupRollDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupRollDialogFragment : BaseDialogFragment() {

    /**********************/
    /** Companion object **/
    /**********************/

    companion object {

        // the fragment initialization parameters
        private const val ARG_GROUP_ID = "group-id"

        // Fragment tags
        const val FT_PERMISSIONS = "permissions"

        /**
         * Use this factory method to create a new instance of
         * this fragment for managing the group with the given [groupId]. Empty means a no group.
         */
        fun newInstance(groupId: String) =
            GroupRollDialogFragment().apply {
                arguments = Bundle().apply { putString(ARG_GROUP_ID, groupId) }
            }
    }

    /************/
    /** Fields **/
    /************/

    override val layoutId = R.layout.fragment_group_roll_dialog
    override lateinit var binding: FragmentGroupRollDialogBinding
    override lateinit var viewModel: GroupRollViewModel
    override val progressBar: ProgressBar? = null

    // TODO make more generic when more permissions are needed
    // Register the permissions callback
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) context?.let { QRCodeScanner.setupCameraView(it) }
            else handlePermissionDenied()
        }

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Init the viewModel using it's factory. If no taleId is found, use empty.
        viewModel = ViewModelProvider(this).get(GroupRollViewModel::class.java)
        binding = getDataBinding(inflater, container)
        binding.viewModel = viewModel
        observeEvents()

        // Set the action bar
        setActionBar(binding.toolbar, StringResource(R.string.group_roll_screen_title))

        return binding.root
    }

    /********************************************/
    /** EventStateHolder.EventObserver methods **/
    /********************************************/

    override fun handleEvent(event: BaseViewModel.BaseEventType) {
        super.handleEvent(event)
        when (event) {
            is GroupRollViewModel.EventType.OpenCameraScanner -> setupCameraView()
        }
    }

    /***********************/
    /** private methods **/
    /***********************/

    // TODO move to generic permission handling when made
    private fun setupCameraView() =
        context?.let {
            when {
                ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED ->
                    QRCodeScanner.setupCameraView(it)
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ->
                    showRequestPermissionRationale()
                else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

    // TODO move to generic permission handling when made
    private fun showRequestPermissionRationale() =
        binding.mainLayout.showSnackbar(
            binding.root,
            getString(R.string.group_roll_permission_rationale_message),
            Snackbar.LENGTH_INDEFINITE,
            getString(R.string.dialog_ok)
        ) {
            requestPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }

    // TODO move to generic permission handling when made
    private fun handlePermissionDenied() =
        AlertDialog<Unit>(
            message = StringResource(getString(R.string.group_roll_permission_denied_message)),
            withNegative = false,
            okButtonText = StringResource(getString(R.string.dialog_ok))
        ).show(parentFragmentManager, FT_PERMISSIONS)

    // TODO move to generic permission handling when made
    private fun View.showSnackbar(
        view: View,
        msg: String,
        length: Int,
        actionMessage: CharSequence?,
        action: (View) -> Unit
    ) {
        val snackbar = Snackbar.make(view, msg, length)
        if (actionMessage != null) {
            snackbar.setAction(actionMessage) {
                action(this)
            }.show()
        } else {
            snackbar.show()
        }
    }
}