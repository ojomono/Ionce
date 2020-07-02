package com.ojomono.ionce.utils

import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    // This extension allows us to use TAG in any class (used for logging)
    val Any.TAG: String
        get() {
            val tag = javaClass.simpleName
            return if (tag.length <= 23) tag else tag.substring(0, 23)
        }

}