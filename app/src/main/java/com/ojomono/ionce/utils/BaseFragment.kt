package com.ojomono.ionce.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
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
    abstract val progressBar: ProgressBar?

    /***********************/
    /** protected methods **/
    /***********************/

    /**
     * Observe possible events of [viewModel]. Implement [handleEvent] in order to use.
     */
    protected fun observeEvents() {
        viewModel.events.observe(viewLifecycleOwner) { it.consume { event -> handleEvent(event) } }
    }

    /**
     * Handle a "raised" event. Implement with: "when(event) { ... }" to handle all possible events.
     */
    protected open fun handleEvent(event: BaseViewModel.Event) {
        when (event) {
            is BaseViewModel.ShowProgressBar -> progressBar?.let { event.task.withProgressBar(it) }
            is BaseViewModel.ShowErrorMessage -> showErrorMessage(event.e.message)
            is BaseViewModel.ShowErrorMessageByResId ->
                showErrorMessage(getString(event.messageResId))
        }
    }

    /***********************/
    /** protected methods **/
    /***********************/

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

    /***********************/
    /** private methods **/
    /***********************/

    /**
     * Show the given error [message] and hide the progress bar.
     */
    private fun showErrorMessage(message: String?) {
        // The error is probably the result of the operation - so make sure the progress bar is gone
        progressBar?.apply { visibility = View.GONE }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
