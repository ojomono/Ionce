package com.ojomono.ionce.utils

import android.view.View
import android.widget.ProgressBar
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

/**
 * This extension allows us to use the "TAG" constant in any class to get the class name (used for
 * logging).
 */
val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }

/**
 * This extension allows us to show the given [progressBar] while the task is running, and hide it
 * when completed.
 */
fun <T> Task<T>.withProgressBar(progressBar: ProgressBar): Task<T> {
    // TODO: keep track on how many tasks are running with the same bar in order to hide it only
    //  when all of them are done.
    progressBar.visibility = View.VISIBLE
    addOnCompleteListener { progressBar.visibility = View.GONE }
    return this
}

/**
 * Continue current [Task] with [continuation] only if current [Task] was successful.
 */
fun <TResult, TContinuationResult> Task<TResult>.continueIfSuccessful(
    continuation: (Task<TResult>) -> Task<TContinuationResult>?
) = continueWithTask { if (!it.isSuccessful) Tasks.forCanceled() else continuation.invoke(it) }

/**
 * Continue current [Task] with [continuation] only if current [Task] failed.
 */
fun <T> Task<T>.addFallbackTask(continuation: (Task<T>) -> Task<*>?) =
    continueWithTask {
        if (!it.isSuccessful) continuation.invoke(it)
        it
    }
