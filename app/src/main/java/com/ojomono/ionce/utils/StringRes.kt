package com.ojomono.ionce.utils

import android.content.Context

/**
 * Used as a wrapper for string resource parameters to allow using res-id or actual string.
 * @author Meztihn (https://www.reddit.com/r/Kotlin/comments/8526gm/optional_param_that_can_be_two_types_string_or/)
 */
sealed class StringRes {
    companion object {
        val EMPTY: StringRes = Complete("")

        operator fun invoke(text: String): StringRes = Complete(text)
        operator fun invoke(id: Int): StringRes = Contextual(id)
    }

    abstract fun inContext(context: Context): String

    data class Contextual(val id: Int) : StringRes() {
        override fun inContext(context: Context): String = context.getString(id)
    }

    data class Complete(val text: String) : StringRes() {
        override fun inContext(context: Context): String = text
    }
}