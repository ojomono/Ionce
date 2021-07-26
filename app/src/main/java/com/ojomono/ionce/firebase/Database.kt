package com.ojomono.ionce.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ojomono.ionce.utils.TAG

/**
 * Proxy object that handles all interactions with Firebase database.
 */
object Database {

    // The Firestore instance
    private val firestore: FirebaseFirestore get() = Firebase.firestore

    // Proxies
    fun collection(collectionPath: String) = firestore.collection(collectionPath)
    fun runBatch(batchFunction: WriteBatch.Function) =
        firestore.runBatch(batchFunction)
            .addOnSuccessListener { Log.d(TAG, "Batch write success!") }
            .addOnFailureListener { e -> Log.w(TAG, "Batch write failure.", e) }

}