package com.ojomono.ionce.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import com.ojomono.ionce.R
import com.ojomono.ionce.utils.StringResource

// TODO consider using 3rd party library instead: https://github.com/afollestad/material-dialogs
/**
 * A simple dialog fragment used for noticing the user and let him approve or cancel an action.
 */
class AlertDialog<T>(
    title: StringResource = StringResource.EMPTY,
    message: StringResource = StringResource.EMPTY,
    onNegative: (() -> Unit)? = null,
    withNegative: Boolean = true,
    private val onPositive: (() -> T)? = null,
    private val okButtonText: StringResource = StringResource(R.string.dialog_save),
) : BaseDialog(title, message, onNegative, withNegative) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        return context?.let { context ->

            // Add the positive button
            builder.setPositiveButton(okButtonText.inContext(context)) { _, _ ->
                onPositive?.invoke()
                dialog?.cancel()
            }

            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Context cannot be null")
    }
}