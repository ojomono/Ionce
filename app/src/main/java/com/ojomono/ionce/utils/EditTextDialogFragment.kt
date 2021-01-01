package com.ojomono.ionce.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.ojomono.ionce.R

class EditTextDialogFragment<T>(
    private val onPositive: (String) -> T,
    private val onNegative: (() -> Unit)? = null,
    private val okButtonText: StringRes = StringRes(R.string.dialog_save),
    private val defaultInputText: StringRes = StringRes.EMPTY,
    private val message: StringRes = StringRes.EMPTY,
    private val title: StringRes = StringRes.EMPTY
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            context?.let { context ->

                // Build input field
                val input = EditText(context)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                input.layoutParams = lp
                if (defaultInputText != StringRes.EMPTY)
                    input.setText(defaultInputText.inContext(context))

                // Use the Builder class for convenient dialog construction
                val builder = AlertDialog.Builder(activity).apply {
                    if (title != StringRes.EMPTY) setTitle(title.inContext(context))
                    if (message != StringRes.EMPTY) setMessage(message.inContext(context))
                    setView(input)
                    setPositiveButton(okButtonText.inContext(context)) { _, _ ->
                        onPositive(input.text.toString())
                        dialog?.cancel()
                    }
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