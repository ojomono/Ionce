package com.ojomono.ionce.ui.roll

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentRollBinding
import com.ojomono.ionce.utils.BaseFragment
import com.ojomono.ionce.utils.BaseViewModel

class RollFragment : BaseFragment() {

    /************/
    /** Fields **/
    /************/

    override val layoutId = R.layout.fragment_roll
    override lateinit var binding: FragmentRollBinding
    override lateinit var viewModel: RollViewModel

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(RollViewModel::class.java)
        binding = getDataBinding(inflater, container)
        binding.viewModel = viewModel
        observeEvents()

        return binding.root
    }

    /**************************/
    /** BaseFragment methods **/
    /**************************/

    override fun handleEvent(event: BaseViewModel.Event) {
        when (event) {
            is RollViewModel.EventType.ShowErrorMessage ->
                Toast.makeText(context, event.messageResId, Toast.LENGTH_SHORT).show()
        }
    }
}