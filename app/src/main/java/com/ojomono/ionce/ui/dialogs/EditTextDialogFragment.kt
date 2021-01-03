package com.ojomono.ionce.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.ojomono.ionce.R
import com.ojomono.ionce.utils.BaseDialogFragment
import com.ojomono.ionce.utils.StringResource


/**
 * A simple dialog fragment used for getting a string input from the user.
 */
class EditTextDialogFragment<T>(
    title: StringResource = StringResource.EMPTY,
    message: StringResource = StringResource.EMPTY,
    onNegative: (() -> Unit)? = null,
    private val onPositive: ((String) -> T)? = null,
    private val okButtonText: StringResource = StringResource(R.string.dialog_save),
    private val defaultInputText: StringResource = StringResource.EMPTY
) : BaseDialogFragment(title, message, onNegative) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        return context?.let { context ->

            // Init the input field and it's default value
            val input = EditText(context)
            if (defaultInputText != StringResource.EMPTY)
                input.setText(defaultInputText.inContext(context))

            // Create the input field (with wanted margins)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                marginEnd = resources.getDimensionPixelSize(R.dimen.dialog_margin)
                marginStart = resources.getDimensionPixelSize(R.dimen.dialog_margin)
            }
            input.layoutParams = lp

            // Activate automatic first letter capitalization
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

            // Add input inside container (in order for the margins to show)
            val container = FrameLayout(context)
            container.addView(input)
            builder.setView(container)

            // Add the positive button
            builder.setPositiveButton(okButtonText.inContext(context)) { _, _ ->
                onPositive?.invoke(input.text.toString())
                dialog?.cancel()
            }

            // Create the AlertDialog object and return it
            builder.create().apply { disablePositiveButtonWhileInputIsEmpty(input) }
        } ?: throw IllegalStateException("Context cannot be null")
    }

    /**
     * Disable positive button while [input] is empty.
     */
    private fun AlertDialog.disablePositiveButtonWhileInputIsEmpty(input: EditText) =
        setOnShowListener {
            // Get the positive button from the dialog
            val positiveButton = (it as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)

            // Initially disable the button
            if (defaultInputText == StringResource.EMPTY) positiveButton.isEnabled = false

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