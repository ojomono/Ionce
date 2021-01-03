package com.ojomono.ionce.utils

import android.content.Context

/**
 * Used as a wrapper for string resource parameters to allow using res-id or actual string.
 * @author Meztihn (https://www.reddit.com/r/Kotlin/comments/8526gm/optional_param_that_can_be_two_types_string_or/)
 */
sealed class StringResource {
    companion object {
        val EMPTY: StringResource = Complete("")

        operator fun invoke(text: String): StringResource = Complete(text)
        operator fun invoke(id: Int): StringResource = Contextual(id)
    }

    abstract fun inContext(context: Context): String

    data class Contextual(val id: Int) : StringResource() {
        override fun inContext(context: Context): String = context.getString(id)
    }

    data class Complete(val text: String) : StringResource() {
        override fun inContext(context: Context): String = text
    }
}