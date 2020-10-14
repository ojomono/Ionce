package com.ojomono.ionce.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

/**
 * A [Fragment] that can observe the [BaseViewModel] events.
 */
abstract class BaseFragment : Fragment() {

    /************/
    /** Fields **/
    /************/

    abstract val layoutId: Int
    abstract val binding: ViewDataBinding
    abstract val viewModel: BaseViewModel

    /**********************/
    /** abstract methods **/
    /**********************/

    /**
     * Handle a "raised" event. Implement with: "when(event) { ... }" to handle all possible events.
     */
    abstract fun handleEvent(event: BaseViewModel.Event)

    /***********************/
    /** protected methods **/
    /***********************/

    /**
     * Observe possible events of [viewModel]. Implement [handleEvent] in order to use.
     */
    protected fun observeEvents() {
        viewModel.events.observe(viewLifecycleOwner) { it.consume { event -> handleEvent(event) } }
    }

    /*********************/
    /** private methods **/
    /*********************/

    /**
     * Init the [binding] field. Notice connecting to ViewModel still needed!
     */
    protected fun <T : ViewDataBinding> getDataBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): T {
        // Inflate view and obtain an instance of the binding class
        val binding: T = DataBindingUtil.inflate(inflater, layoutId, container, false)

        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = viewLifecycleOwner

        return binding
    }
}
