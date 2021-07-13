package com.ojomono.ionce.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment

// TODO maybe find a way to combine with BaseFragment class to an interface
/**
 * A full-screen [DialogFragment] that can observe the [BaseViewModel] events.
 */
abstract class BaseDialogFragment : FullScreenDialogFragment(),
    EventStateHolder.EventObserver<BaseViewModel.BaseEventType> {

    /************/
    /** Fields **/
    /************/

    abstract val layoutId: Int
    abstract val binding: ViewDataBinding
    abstract val viewModel: BaseViewModel
    abstract val progressBar: ProgressBar?

    /********************************************/
    /** EventStateHolder.EventObserver methods **/
    /********************************************/

    /**
     * Handle a "raised" event. Implement with: "when(event) { ... }" to handle all possible events.
     */
    override fun handleEvent(event: BaseViewModel.BaseEventType) {
        when (event) {
            is BaseViewModel.BaseEventType.ShowProgressBar ->
                progressBar?.let { event.task.withProgressBar(it) }
            is BaseViewModel.BaseEventType.ShowErrorMessage ->
                showMessage(event.e.message)
            is BaseViewModel.BaseEventType.ShowMessageByResId ->
                showMessage(getString(event.messageResId, *event.args))
        }
    }

    /***********************/
    /** protected methods **/
    /***********************/

    /**
     * Observe possible events of [viewModel]. Implement [handleEvent] in order to use. Handling
     * only events that inherit from [BaseViewModel.BaseEventType].
     */
    protected fun observeEvents() =
        viewModel.events.observeEvents(viewLifecycleOwner, this)

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
     * Show the given [message].
     */
    private fun showMessage(message: String?) =
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}