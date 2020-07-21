package com.ojomono.ionce.firebase

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Handles all interactions with Firebase database.
 */
object Database {

    // The Cloud Firestore instance
    val db = Firebase.firestore

}