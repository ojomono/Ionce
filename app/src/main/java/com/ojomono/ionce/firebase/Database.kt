package com.ojomono.ionce.firebase

import android.util.Log
import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ojomono.ionce.models.UserModel
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

    /************/
    /** Fields **/
    /************/

    // The Firestore instance
    private val firestore: FirebaseFirestore get() = Firebase.firestore

    // Always keep current user's document loaded
    var userDocRef: DocumentReference? = null
    private var userDocument: DocumentSnapshot? = null
    private var userRegistration: ListenerRegistration? = null

    // TODO replace liveData with callbackFlow / StateFlow when they become non-experimental
    //  https://medium.com/firebase-tips-tricks/how-to-use-kotlin-flows-with-firestore-6c7ee9ae12f3

    // Current user data
    private val _userData = MutableLiveData<UserModel>()
    val userData: LiveData<UserModel> = _userData

    init {
        Authentication.currentUser.observeForever { switchUserDocument(it?.uid) }
    }

    /********************/
    /** public methods **/
    /********************/

    fun collection(collectionPath: String) = firestore.collection(collectionPath)
    fun runBatch(batchFunction: WriteBatch.Function) =
        firestore.runBatch(batchFunction)
            .addOnSuccessListener { Log.d(TAG, "Batch write success!") }
            .addOnFailureListener { e -> Log.w(TAG, "Batch write failure.", e) }

    /*********************/
    /** private methods **/
    /*********************/

    /**
     * Switch the current user document reference and snapshot to those of the user with the given
     * [id] - and listen to changes. Return the get [Task].
     */
    private fun switchUserDocument(id: String?): Task<DocumentSnapshot>? {

        var task: Task<DocumentSnapshot>? = null

        // If no id was given - no user is logged-in
        if (id.isNullOrEmpty()) {
            userRegistration?.remove()
            userDocRef = null
            userDocument = null

            // If the new id belongs to another user than the one we currently refer to
        } else if (!userDocRef?.id.equals(id)) {

            // If a change listener is registered to the previous user's document - remove it
            userRegistration?.remove()

            // Get the current user's document reference
            userDocRef = firestore.collection(CP_USERS).document(id)

            // Save the document locally
            task = userDocRef?.get()
            task?.continueWithTask { getTask ->
                userDocument = getTask.result
                // If the document does not exist yet - initialize it, as a continuation task
                if (userDocument?.exists() == true) null
                else userDocRef?.set(UserModel())
            }?.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

            // Listen for changes in the document
            userDocRef?.addSnapshotListener { snapshot, e ->
                if (e != null) Log.w(TAG, "Listen failed.", e)
                else {
                    userDocument = snapshot
                    _userData.value = snapshot?.toObject(UserModel::class.java)
                }
            }
        }

        return task
    }
}