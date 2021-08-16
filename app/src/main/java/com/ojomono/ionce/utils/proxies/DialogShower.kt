package com.ojomono.ionce.utils.proxies

import android.content.Context
import android.text.InputType
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.InputCallback
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.MultiChoiceListener
import com.afollestad.materialdialogs.list.listItemsMultiChoice

/**
 * Handles all dialog showing - wrapping MaterialDialog.
 */
object DialogShower {

    fun show(context: Context?, func: MaterialDialog.() -> Unit) =
        context?.let { MaterialDialog(it).show(func) }

    // "input" extension wrapper
    fun MaterialDialog.withInput(
        hint: String? = null,
        @StringRes hintRes: Int? = null,
        prefill: CharSequence? = null,
        @StringRes prefillRes: Int? = null,
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        maxLength: Int? = null,
        waitForPositiveButton: Boolean = true,
        allowEmpty: Boolean = false,
        callback: InputCallback = null
    ) = input(
        hint,
        hintRes,
        prefill,
        prefillRes,
        inputType,
        maxLength,
        waitForPositiveButton,
        allowEmpty,
        callback
    )

    fun MaterialDialog.withListItemsMultiChoice(
        @ArrayRes res: Int? = null,
        items: List<CharSequence>? = null,
        disabledIndices: IntArray? = null,
        initialSelection: IntArray = IntArray(0),
        waitForPositiveButton: Boolean = true,
        allowEmptySelection: Boolean = false,
        selection: MultiChoiceListener = null
    ) = listItemsMultiChoice(
        res,
        items,
        disabledIndices,
        initialSelection,
        waitForPositiveButton,
        allowEmptySelection,
        selection
    )
}