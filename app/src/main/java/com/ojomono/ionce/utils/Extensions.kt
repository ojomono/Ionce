package com.ojomono.ionce.utils

import android.app.AlertDialog
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.google.android.gms.tasks.Task
import com.ojomono.ionce.R

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
