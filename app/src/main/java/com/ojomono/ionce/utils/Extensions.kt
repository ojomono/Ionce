package com.ojomono.ionce.utils

import android.view.View
import android.widget.ProgressBar
import com.google.android.gms.tasks.Task

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
    progressBar.visibility = View.VISIBLE
    addOnCompleteListener { progressBar.visibility = View.GONE }
    return this
}
