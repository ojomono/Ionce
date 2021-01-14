package com.ojomono.ionce.firebase

// TODO: Avoid Android imports and move to separated module when needed for more UI platforms
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ojomono.ionce.models.TaleModel
import com.ojomono.ionce.models.UserModel
import com.ojomono.ionce.models.TaleItemModel
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

    /************/
    /** Fields **/
    /************/

    // The Cloud Firestore instance
    private val db = Firebase.firestore

    // Always keep current user's document loaded
    private var userDocRef: DocumentReference? = null
    private var userDocument: DocumentSnapshot? = null
    private var registration: ListenerRegistration? = null

    init {
        Authentication.currentUser.observeForever { switchUserDocument(it?.uid) }
    }

    // TODO replace liveData with callbackFlow / StateFlow when they become non-experimental
    //  https://medium.com/firebase-tips-tricks/how-to-use-kotlin-flows-with-firestore-6c7ee9ae12f3

    // Current user's tales list
    private val _userTales = MutableLiveData<MutableList<TaleItemModel>>()
    val userTales: LiveData<MutableList<TaleItemModel>> = _userTales

    /********************/
    /** public methods **/
    /********************/

    /**
     * Switch the current user document reference and snapshot to those of the user with the given
     * [id] - and listen to changes. Return the get [Task].
     */
    private fun switchUserDocument(id: String?): Task<DocumentSnapshot>? {

        var task: Task<DocumentSnapshot>? = null

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
            registration = userDocRef?.addSnapshotListener() { snapshot, e ->
                if (e != null) Log.w(TAG, "Listen failed.", e)
                else {
                    userDocument = snapshot
                    _userTales.value = snapshot?.toObject(UserModel::class.java)?.tales
                }
            }
        }

        return task
    }

    /**
     * Get the tale document with id=[id].
     */
    fun getTale(id: String): Task<DocumentSnapshot>? {
        val docRef = userDocRef?.collection(CP_TALES)?.document(id)
        return docRef?.get()
            ?.addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            ?.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    /**
     * Overwrite the matching tale document with the given [tale]. If [tale].id is EMPTY, create
     * a new document with a generated id. Return the set [Task].
     */
    fun setTale(tale: TaleModel): Task<Void>? {
        var task: Task<Void>? = null

        // Setting a tale is possible only if a user is logged in
        // (== his document reference is not null)
        userDocRef?.let { userRef ->
            // Create reference for wanted tale, for use inside the batch - if an id was given
            // get the existing document, else reference a new one.
            val talesCol = userRef.collection(CP_TALES)
            val taleRef = if (tale.id.isEmpty()) talesCol.document() else talesCol.document(tale.id)

            // Create a taleItem based on the given tale, to update the user's tales list
            val taleItem = TaleItemModel(tale, taleRef.id)

            // Create an updated user's tales list
            userTales.value?.let {
                val tales = it.toMutableList().apply {
                    // If given taleItem is in the list (searched by id) - overwrite it with new
                    // model, else add it
                    val index = indexOfFirst { item -> item.id == taleItem.id }
                    if (index == -1) add(taleItem)
                    else this[index] = taleItem
                }

                // In a batch write, set the tale's document and update the user's tale list
                task = db.runBatch { batch ->
                    batch.update(userRef, UserModel::tales.name, tales)
                    batch.set(taleRef, tale)
                }
            }
            task?.addOnSuccessListener { Log.d(TAG, "Batch write success!") }
                ?.addOnFailureListener { e -> Log.w(TAG, "Batch write failure.", e) }
        }

        return task
    }

    /**
     * Delete the tale document with id=[id].
     */
    fun deleteTale(id: String): Task<Void>? {
        var task: Task<Void>? = null

        // Deleting a tale is possible only if a user is logged in
        // (== his document reference is not null)
        userDocRef?.let { userRef ->
            // Create reference for wanted tale, for use inside the transaction
            val taleRef = userRef.collection(CP_TALES).document(id)

            // Create an updated user's tales list
            userTales.value?.let {
                val tales = it.toMutableList().apply {
                    // If given id is in the list - delete it
                    val index = indexOfFirst { item -> item.id == id }
                    if (index != -1) removeAt(index)
                }

                // In a transaction, delete the tale document and remove it from user's tales list
                task = db.runBatch { batch ->
                    batch.update(userRef, UserModel::tales.name, tales)
                    batch.delete(taleRef)
                }
            }

            task?.addOnSuccessListener { Log.d(TAG, "Batch write success!") }
                ?.addOnFailureListener { e -> Log.w(TAG, "Batch write failure.", e) }
        }

        return task
    }

    // For drag n' drop feature
//    /**
//     * Update the current users tales list so it holds the current tales order.
//     */
//    fun saveTalesOrder() =
//        userDocRef?.update(FN_TALES, userTales.value)
//            ?.addOnSuccessListener { Log.d(TAG, "userDocRef successfully updated!") }
//            ?.addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }

}