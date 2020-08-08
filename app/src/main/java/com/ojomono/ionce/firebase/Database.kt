package com.ojomono.ionce.firebase

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ojomono.ionce.models.Tale
import com.ojomono.ionce.models.User
import com.ojomono.ionce.models.TalesItem
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

    /*************/
    /** Members **/
    /*************/

    // The Cloud Firestore instance
    private val db = Firebase.firestore

    // Current user's document
    private var userDocRef: DocumentReference? = null
    private var userDocument: DocumentSnapshot? = null
    private var registration: ListenerRegistration? = null

    // Current user's talesItems
    var userTales: MutableLiveData<List<TalesItem>> = MutableLiveData()

    // If a user is already logged-in - get it's document
    init {
        Authentication.getCurrentUser()?.uid?.let { switchUserDocument(it) }
    }

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
                else {
                    userDocument = snapshot
                    userTales.value = snapshot?.toObject(User::class.java)?.talesItems
                }
            }
        }
    }

    /******************/
    /** CRUD methods **/
    /******************/

    // TODO: Define security rules in Firestore:
    // https://codelabs.developers.google.com/codelabs/firestore-android/#7
    // https://console.firebase.google.com/u/1/project/ionce-9e4c3/database/firestore/rules

    fun setTale(id: String = "", title: String) {
        // Setting tale is possible only if a user is logged in
        // (== his document reference is not null)
        userDocRef?.let { userRef ->
            val talesCol = userRef.collection(CP_TALES)
            // Create reference for wanted tale, for use inside the transaction - if an id was given
            // get the existing document, else reference a new one.
            val taleRef =
                if (id.isEmpty()) talesCol.document() else talesCol.document(id)
            val tale = Tale(taleRef.id, title)

            // In a transaction, set the tale's document and update the user's tale list
            db.runTransaction { transaction ->
                val user: User? = transaction.get(userRef).toObject(User::class.java)
                user?.let {
                    // Add new tale to user's list
                    user.setTale(TalesItem(tale))

                    // Commit to Firestore
                    transaction.set(userRef, user)
                    transaction.set(taleRef, tale)
                }
            }
        }
    }

    fun deleteTale(id: String) {

    }
}