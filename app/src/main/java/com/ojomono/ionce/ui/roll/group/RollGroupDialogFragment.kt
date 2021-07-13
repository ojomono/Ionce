package com.ojomono.ionce.ui.roll.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentRollGroupDialogBinding
import com.ojomono.ionce.utils.BaseDialogFragment

/**
 * A [DialogFragment] representing the management screen for a roll group.
 * Use the [RollGroupDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RollGroupDialogFragment : BaseDialogFragment() {

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
            RollGroupDialogFragment().apply {
                arguments = Bundle().apply { putString(ARG_GROUP_ID, groupId) }
            }
    }

    /************/
    /** Fields **/
    /************/

    override val layoutId = R.layout.fragment_roll_group_dialog
    override lateinit var binding: FragmentRollGroupDialogBinding
    override lateinit var viewModel: RollGroupViewModel
    override val progressBar: ProgressBar? = null

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutId, container, false)
    }
}