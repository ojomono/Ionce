package com.ojomono.ionce.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.ojomono.ionce.R

// TODO consider using 3rd party library instead: https://github.com/afollestad/material-dialogs
/**
 * A [DialogFragment] that prepares an AlertDialog.Builder for building a basic dialog with optional
 * [title], [message] and [onNegative] button action.
 */
abstract class BaseDialogFragment(
    private val title: StringResource = StringResource.EMPTY,
    private val message: StringResource = StringResource.EMPTY,
    private val onNegative: (() -> Unit)? = null
) : DialogFragment() {

    protected lateinit var builder: AlertDialog.Builder

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            context?.let { context ->

                // Use the Builder class for convenient dialog construction
                builder = AlertDialog.Builder(activity).apply {
                    if (title != StringResource.EMPTY) setTitle(title.inContext(context))
                    if (message != StringResource.EMPTY) setMessage(message.inContext(context))
                    setNegativeButton(R.string.dialog_cancel) { _, _ ->
                        onNegative?.invoke()
                        dialog?.cancel()
                    }
                }

                // Create the AlertDialog object and return it
                builder.create()
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}