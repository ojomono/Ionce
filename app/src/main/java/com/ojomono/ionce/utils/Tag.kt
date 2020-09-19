package com.ojomono.ionce.utils

/**
 * This extension allows us to use the "TAG" constant in any class to get the class name (used for
 * logging).
 */
val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }