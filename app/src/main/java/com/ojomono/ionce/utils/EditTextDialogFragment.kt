package com.ojomono.ionce.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.EditText
import android.widget.FrameLayout
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

                // Init the input field and it's default value
                val input = EditText(context)
                if (defaultInputText != StringRes.EMPTY)
                    input.setText(defaultInputText.inContext(context))

                // Add the input field with wanted margins
                val container = FrameLayout(activity)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    marginEnd = resources.getDimensionPixelSize(R.dimen.dialog_margin)
                    marginStart = resources.getDimensionPixelSize(R.dimen.dialog_margin)
                }
                input.layoutParams = lp
                container.addView(input)

                // Use the Builder class for convenient dialog construction
                val builder = AlertDialog.Builder(activity).apply {
                    if (title != StringRes.EMPTY) setTitle(title.inContext(context))
                    if (message != StringRes.EMPTY) setMessage(message.inContext(context))
                    setView(container)
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
                builder.create().apply { disablePositiveButtonWhileInputIsEmpty(input) }
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    /**
     * Disable positive button while [input] is empty.
     */
    private fun AlertDialog.disablePositiveButtonWhileInputIsEmpty(input: EditText) =
        setOnShowListener {
            // Get the positive button from the dialog
            val positiveButton = (it as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)

            // Initially disable the button
            if (defaultInputText == StringRes.EMPTY) positiveButton.isEnabled = false

            // Set the textChanged listener for editText
            input.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun afterTextChanged(s: Editable) {
                    // Disable button when input is empty
                    positiveButton.isEnabled = !TextUtils.isEmpty(s)
                }
            })
        }
}