package com.ojomono.ionce.firebase

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ojomono.ionce.ui.tales.Tale
import java.util.ArrayList

/**
 * Handles all interactions with Firebase database.
 */
object Database {
    // The Cloud Firestore instance
    private val db = Firebase.firestore

    fun getUserTales(): List<Tale>? {
//        TODO("Not yet implemented")
        val dummy = ArrayList<Tale>()
        dummy.add(Tale(1, "Tale 1"))
        dummy.add(Tale(2, "Tale 2"))
        return dummy
    }
}