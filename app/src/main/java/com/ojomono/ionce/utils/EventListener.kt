package com.ojomono.ionce.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Holds events as part of it's state and allows views to observe those events.
 */
class EventListener {

    /**
     * The observable event holder.
     */
    @JvmSynthetic
    @PublishedApi
    internal val events = MutableLiveData<OneTimeEvent<Event>>()

    /**
     * Post the given [event] to the observable holder. Call to "raise" the event.
     */
    fun postEvent(event: Event) = events.postValue(OneTimeEvent(event))

    /**
     * Observe from view class to "catch" the events of type [T].
     */
    inline fun <reified T : Event> observeEvents(
        owner: LifecycleOwner,
        observer: EventObserver<T>
    ) =
        events.observe(owner) {
            it.consume { event -> if (event is T) observer.handleEvent(event) }
        }

    /**
     * Implement this to be able to observe the events.
     */
    interface EventObserver<T : Event> {
        fun handleEvent(event: T)
    }

    /**
     * The basic event type. Implement with a sealed class to allow different event types.
     */
    interface Event

    /**
     * Used as a wrapper for model that is exposed via a LiveData that represents an event.
     * @author aminography (https://gist.github.com/JoseAlcerreca/e0bba240d9b3cffa258777f12e5c0ae9)
     * referenced from: https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
     */
    class OneTimeEvent<out T>(private val value: T) {

        private val isConsumed = AtomicBoolean(false)

        internal fun getValue(): T? =
            if (isConsumed.compareAndSet(false, true)) value
            else null

        fun consume(block: (T) -> Unit): T? = getValue()?.also(block)
    }

}