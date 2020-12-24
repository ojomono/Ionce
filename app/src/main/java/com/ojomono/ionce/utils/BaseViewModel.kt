package com.ojomono.ionce.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import java.lang.Exception

/**
 * A [ViewModel] that holds events as part of it's state. It allows views to observe those events.
 */
abstract class BaseViewModel : ViewModel() {

    // The basic event type. Implement with a sealed class to allow different event types.
    interface Event

    // Common event types
    sealed class EventType() : Event {
        class ShowProgressBar(val task: Task<*>) : EventType()
        class ShowErrorMessage(val e: Exception) : EventType()
        class ShowMessageByResId(val messageResId: Int, vararg val args: String) : EventType()
    }

    // The observable event holder. Observe from view class to "catch" the events.
    private val _events = MutableLiveData<OneTimeEvent<Event>>()
    val events: LiveData<OneTimeEvent<Event>> = _events

    /**
     * Post the given [event] to the observable holder. Call to "raise" the event.
     */
    protected fun postEvent(event: Event) {
        _events.postValue(OneTimeEvent(event))
    }

    // Common event posting aliases
    protected fun Task<*>.withProgressBar() =
        apply { postEvent(EventType.ShowProgressBar(this)) }

    protected fun showErrorMessage(e: Exception) = postEvent(EventType.ShowErrorMessage(e))
    protected fun showMessageByResId(resId: Int, vararg args: String) =
        postEvent(EventType.ShowMessageByResId(resId, *args))

}