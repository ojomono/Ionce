package com.ojomono.ionce.utils

import androidx.fragment.app.Fragment

/**
 * A [Fragment] that can observe the [BaseViewModel] events.
 */
abstract class BaseFragment : Fragment() {

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
}
