package com.ojomono.ionce.ui.roll.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentGroupRollDialogBinding
import com.ojomono.ionce.ui.bases.BaseDialogFragment
import com.ojomono.ionce.utils.StringResource
import com.ojomono.ionce.utils.TAG
import com.ojomono.ionce.ui.bases.BaseViewModel

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

        // Constants for setFragmentResultListener
        private const val RK_GROUP_ID = "group-id"
        private const val BK_GROUP_ID = "group-id"

        /**
         * Use this factory method to create a new instance of this fragment.
         */
        fun newInstance() = GroupRollDialogFragment()
    }

    /************/
    /** Fields **/
    /************/

    override val layoutId = R.layout.fragment_group_roll_dialog
    override lateinit var binding: FragmentGroupRollDialogBinding
    override lateinit var viewModel: GroupRollViewModel
    override val progressBar: ProgressBar? = null

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
            is GroupRollViewModel.EventType.OpenQRCodeScanner -> openQRCodeScanner()
        }
    }

    /***********************/
    /** private methods **/
    /***********************/

    private fun openQRCodeScanner() {
        setFragmentResultListener(RK_GROUP_ID) { _, bundle ->
            val result = bundle.getString(BK_GROUP_ID)
            if (result != null) {
                viewModel.joinGroup(result)
            }
        }
        QRCodeScannerDialogFragment.newInstance(RK_GROUP_ID, BK_GROUP_ID)
            .let { it.show(parentFragmentManager, it.TAG) }
    }

}