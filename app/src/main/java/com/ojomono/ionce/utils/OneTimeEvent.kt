package com.ojomono.ionce.utils

import java.util.concurrent.atomic.AtomicBoolean

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
