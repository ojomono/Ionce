package com.ojomono.ionce.firebase

import android.util.Log
import com.google.firebase.firestore.*
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ojomono.ionce.models.User
import com.ojomono.ionce.models.Tale
import com.ojomono.ionce.utils.TAG


/**
 * Handles all interactions with Firebase database.
 */
object Database {

    /***************/
    /** Constants **/
    /***************/

    // Collection Paths
    private const val CP_USERS = "users"
    private const val CP_TALES = "tales"

    // Hash map keys
    private const val KEY_TITLE = "title"

    /*************/
    /** Members **/
    /*************/

    // The Cloud Firestore instance
    private val db = Firebase.firestore

    // Current user's document
    private var userDocRef: DocumentReference? = null
    private var userDocument: DocumentSnapshot? = null
    private var registration: ListenerRegistration? = null

    init {
        Authentication.getCurrentUser()?.uid?.let { switchUserDocument(it) }
    }

    // Current user's tales
    var userTales: List<Tale> = listOf()

    /**
     * Switch the current user document reference and snapshot to those of the user with the given
     * [id] - and listen to changes.
     */
    fun switchUserDocument(id: String?) {

        // If no id was given - no user is logged-in
        if (id.isNullOrEmpty()) {
            registration?.remove()
            userDocRef = null
            userDocument = null

            // If the new id belongs to another user than the one we currently refer to
        } else if (!userDocRef?.id.equals(id)) {

            // If a change listener is registered to the previous user's document - remove it
            registration?.remove()

            // Get the current user's document reference
            userDocRef = db.collection(CP_USERS).document(id)

            // Save the document locally
            userDocRef?.get()?.addOnSuccessListener { document ->
                userDocument = document
                // If the document does not exist yet - initialize it
                if (!document.exists()) userDocRef?.set(User())
            }?.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

            // Listen for changes in the document
            registration = userDocRef?.addSnapshotListener { snapshot, e ->
                if (e != null) Log.w(TAG, "Listen failed.", e)
                else userDocument = snapshot
            }
        }
    }

    /******************/
    /** CRUD methods **/
    /******************/

    fun addTale(title: String) {
        // Adding tale is possible only if a user is logged in
        // (== his document reference is not null)
        userDocRef?.let { userRef ->
            // Create reference for new tale, for use inside the transaction
            val taleRef = userRef.collection(CP_TALES).document()
            val tale = Tale(taleRef.id, title)

            // In a transaction, add the new tale and update the user's list
            db.runTransaction { transaction ->
                val user: User? = transaction.get(userRef).toObject(User::class.java)
                user?.let {
                    // Add new tale to user's list
                    user.addTale(tale)

                    // Commit to Firestore
                    transaction.set(userRef, user)
                    transaction.set(taleRef, tale)
                }
            }
        }
    }
}