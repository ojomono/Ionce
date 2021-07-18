package com.ojomono.ionce.ui.roll.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentGroupRollDialogBinding
import com.ojomono.ionce.utils.bases.BaseDialogFragment
import com.ojomono.ionce.utils.StringResource

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
}