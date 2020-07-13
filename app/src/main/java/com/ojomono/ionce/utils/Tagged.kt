package com.ojomono.ionce.utils

/**
 * This extension allows us to use TAG in any class (used for logging).
 */
interface Tagged {
    val Any.TAG: String
        get() {
            val tag = javaClass.simpleName
            return if (tag.length <= 23) tag else tag.substring(0, 23)
        }
}