package com.ojomono.ionce.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task

/**
 * A [ViewModel] that holds events as part of it's state. It allows views to observe those events.
 */
abstract class BaseViewModel : ViewModel() {

    // The basic event type. Implement with a sealed class to allow different event types.
    interface Event

    // Common event types
    class ShowProgressBar(val task: Task<*>) : Event
    class ShowErrorMessage(val messageResId: Int) : Event

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
    protected fun Task<*>.withProgressBar() = apply { postEvent(ShowProgressBar(this)) }
    protected fun showErrorMessage(messageResId: Int) = postEvent(ShowErrorMessage(messageResId))
}