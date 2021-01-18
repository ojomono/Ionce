package com.ojomono.ionce.utils

import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import java.lang.Exception

/**
 * A [ViewModel] with an event listener that holds events as part of it's state and allows views to
 * observe those events.
 */
abstract class BaseViewModel : ViewModel() {

    // Wrap the event listener
    val events = EventStateHolder()
    protected fun postEvent(event: EventStateHolder.Event) = events.postEvent(event)

    /**********************************/
    /** Common event posting aliases **/
    /**********************************/

    // Common event types
    abstract class BaseEventType : EventStateHolder.Event {
        class ShowProgressBar(val task: Task<*>) : BaseEventType()
        class ShowErrorMessage(val e: Exception) : BaseEventType()
        class ShowMessageByResId(val messageResId: Int, vararg val args: String) : BaseEventType()
    }

    protected fun Task<*>.withProgressBar() =
        apply { postEvent(BaseEventType.ShowProgressBar(this)) }

    protected fun showErrorMessage(e: Exception) =
        postEvent(BaseEventType.ShowErrorMessage(e))

    protected fun showMessageByResId(resId: Int, vararg args: String) =
        postEvent(BaseEventType.ShowMessageByResId(resId, *args))

}