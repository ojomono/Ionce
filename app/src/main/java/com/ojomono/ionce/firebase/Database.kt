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
object Database : EventListener<QuerySnapshot> {

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
    private var userReference: DocumentReference? = null

    init {
        Authentication.getCurrentUser()?.uid?.let { switchUserDocument(it) }
    }

    // Current user's tales
    var userTales: List<Tale> = listOf()

    // Get the current user's tales
    var snapshots = mutableListOf<QueryDocumentSnapshot>()
    private val query = userReference?.collection(CP_TALES)
    private var registration = query?.addSnapshotListener(this)

    fun switchUserDocument(id: String?) {
        if (id.isNullOrEmpty()) userReference = null
        else {
            userReference = db.collection(CP_USERS).document(id)
            userReference?.get()?.addOnSuccessListener { document ->
                if (!document.exists()) userReference?.set(User())
            }?.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        }
    }

    /******************/
    /** CRUD methods **/
    /******************/

    fun addTale(title: String) {
        // Adding tale is possible only if a user is logged in
        // (== his document reference is not null)
        userReference?.let { userRef ->
            // Create reference for new tale, for use inside the transaction
            val taleRef = userRef.collection(CP_TALES).document()
            val tale = Tale(taleRef.id, title)

            // In a transaction, add the new tale and update the user's list
            db.runTransaction { transaction ->
                // TODO create user document after sign in
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

    /******************************************/
    /** EventListener<QuerySnapshot> methods **/
    /******************************************/

    override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {

        // Handle errors
        if (error != null) {
            Log.w(TAG, "onEvent:error", error)
            return
        }

        // Dispatch the event
        value?.documentChanges?.let {
            for (change in it) {
                // Snapshot of the changed document
                val snapshot: DocumentSnapshot = change.document
                when (change.type) {
                    DocumentChange.Type.ADDED -> onDocumentAdded(change)
                    DocumentChange.Type.MODIFIED -> onDocumentModified(change)
                    DocumentChange.Type.REMOVED -> onDocumentRemoved(change)
                }
            }
        }

//        onDataChanged()
    }

    private fun onDocumentAdded(change: DocumentChange) {
        snapshots.add(change.newIndex, change.document)
//        notifyItemInserted(change.newIndex)
    }

    private fun onDocumentModified(change: DocumentChange) {
        if (change.oldIndex == change.newIndex) {
            // Item changed but remained in same position
            snapshots[change.oldIndex] = change.document
//            notifyItemChanged(change.oldIndex)
        } else {
            // Item changed and changed position
            snapshots.removeAt(change.oldIndex)
            snapshots.add(change.newIndex, change.document)
//            notifyItemMoved(change.oldIndex, change.newIndex)
        }
    }

    private fun onDocumentRemoved(change: DocumentChange) {
        snapshots.removeAt(change.oldIndex)
//        notifyItemRemoved(change.oldIndex)
    }


// TODO Delete section:

//    fun addTale(title: String) {
//        Authentication.getCurrentUser()?.uid?.let {
//
//            // Create a new tale with the given title
//            val tale = hashMapOf(KEY_TITLE to title)
//
//            // Add a new document with a generated ID
//            db.collection(CP_USERS).document(it).collection(CP_TALES).add(tale)
//                .addOnSuccessListener { documentReference ->
//                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
//                }
//                .addOnFailureListener { e ->
//                    Log.w(TAG, "Error adding document", e)
//                }
//        }
//    }

//    fun getUserTales(): List<Tale>? {
////        TODO("Not yet implemented")
//        val dummy = ArrayList<Tale>()
//        dummy.add(Tale(1, "Tale 1"))
//        dummy.add(Tale(2, "Tale 2"))
//        return dummy
//    }
}