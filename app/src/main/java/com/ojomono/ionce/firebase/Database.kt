package com.ojomono.ionce.firebase

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ojomono.ionce.ui.tales.TaleItemModel
import java.util.ArrayList

/**
 * Handles all interactions with Firebase database.
 */
object Database {
    // The Cloud Firestore instance
    val db = Firebase.firestore

    fun getUserTales(): List<TaleItemModel>? {
//        TODO("Not yet implemented")
        val dummy = ArrayList<TaleItemModel>()
        dummy.add(TaleItemModel(1, "Tale 1"))
        dummy.add(TaleItemModel(2, "Tale 2"))
        return dummy
    }
}