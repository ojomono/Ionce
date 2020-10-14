package com.ojomono.ionce.ui.roll

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
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
        // Init the ViewModel
        viewModel = ViewModelProvider(this).get(RollViewModel::class.java)

        // Init the DataBinding
        initDataBinding(inflater, container)

        // Observe possible events
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

    /*********************/
    /** private methods **/
    /*********************/

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
}