package com.ojomono.ionce.ui.roll.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentRollBinding
import com.ojomono.ionce.ui.roll.group.GroupRollDialogFragment
import com.ojomono.ionce.ui.bases.BaseFragment
import com.ojomono.ionce.ui.bases.BaseViewModel
import com.ojomono.ionce.utils.TAG

class RollFragment : BaseFragment() {

    /************/
    /** Fields **/
    /************/

    override val layoutId = R.layout.fragment_roll
    override lateinit var binding: FragmentRollBinding
    override lateinit var viewModel: RollViewModel
    override val progressBar: ProgressBar? = null

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(RollViewModel::class.java)
        binding = getDataBinding(inflater, container)
        binding.viewModel = viewModel
        observeEvents()

        return binding.root
    }

    /**************************/
    /** BaseFragment methods **/
    /**************************/

    override fun handleEvent(event: BaseViewModel.BaseEventType) {
        super.handleEvent(event)
        when (event) {
            is RollViewModel.EventType.ShowRollGroupDialog -> showRollGroupDialog()
        }
    }

    /*********************/
    /** private methods **/
    /*********************/

    /**
     * Show dialog for managing the roll group.
     */
    private fun showRollGroupDialog() =
        GroupRollDialogFragment.newInstance().let { it.show(parentFragmentManager, it.TAG) }

}