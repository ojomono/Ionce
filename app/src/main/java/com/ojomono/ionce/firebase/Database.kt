package com.ojomono.ionce.firebase

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ojomono.ionce.ui.tales.Tale
import com.ojomono.ionce.utils.TAG
import java.util.ArrayList

/**
 * Handles all interactions with Firebase database.
 */
object Database {

    // Collection Paths
    private const val CP_USERS = "users"
    private const val CP_TALES = "tales"

    // Hash map keys
    private const val KEY_TITLE = "title"

    // The Cloud Firestore instance
    private val db = Firebase.firestore

    /**
     * Create a new tale document in the db with the given [title].
     */
    fun addTale(title: String) {
        Authentication.getCurrentUser()?.uid?.let {

            // Create a new tale with the given title
            val tale = hashMapOf(KEY_TITLE to title)

            // Add a new document with a generated ID
            db.collection(CP_USERS).document(it).collection(CP_TALES).add(tale)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
        }
    }

    fun getUserTales(): List<Tale>? {
//        TODO("Not yet implemented")
        val dummy = ArrayList<Tale>()
        dummy.add(Tale(1, "Tale 1"))
        dummy.add(Tale(2, "Tale 2"))
        return dummy
    }
}