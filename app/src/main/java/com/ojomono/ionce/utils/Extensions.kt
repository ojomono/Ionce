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

/**
 * This extension allows us to add a text input field (with default text = [defaultInputText]) and
 * a save button to save it's final value using the given [onSave] function and showing the given
 * [progressBar] until save is done.
 */
fun AlertDialog.Builder.setInputAndCustomButton(
    onSave: (String) -> Task<*>?,
    progressBar: ProgressBar,
    defaultInputText: String = ""
): AlertDialog.Builder {
    // Set input field
    val input = setInputView(defaultInputText)

    // Add the save button
    return setPositiveButton(context.getText(R.string.dialogs_positive_button_text)) { dialog, _ ->
        onSave(input.text.toString())?.withProgressBar(progressBar)
        dialog.cancel()
    }
}

/**
 * This extension allows us to add a text input field (with default text = [defaultInputText]) and
 * a custom button to use it's final value using the given [onPositive].
 */
fun AlertDialog.Builder.setInputAndCustomButton(
    onPositive: (String) -> Unit,
    buttonTextResId: Int,
    defaultInputText: String = ""
): AlertDialog.Builder {
    // Set input field
    val input = setInputView(defaultInputText)

    // Add the save button
    return setPositiveButton(context.getText(buttonTextResId)) { dialog, _ ->
        onPositive(input.text.toString())
        dialog.cancel()
    }
}

/**
 * This extension is used to set a basic input view dialog with default text = [defaultInputText].
 */
private fun AlertDialog.Builder.setInputView(defaultInputText: String = ""): EditText {
    // Build input field
    val input = EditText(context)
    val lp = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT
    )
    input.layoutParams = lp
    if (defaultInputText.isNotEmpty()) input.setText(defaultInputText)

    // Add the input field and return it
    setView(input)
    return input
}

/**
 * This extension allows us to add a cancel button with optional functionality [onCancel].
 */
fun AlertDialog.Builder.setCancelButton(onCancel: (() -> Unit)? = null): AlertDialog.Builder =
    setNegativeButton(context.getText(R.string.dialogs_negative_button_text)) { dialog, _ ->
        if (onCancel != null) onCancel()
        dialog.cancel()
    }
