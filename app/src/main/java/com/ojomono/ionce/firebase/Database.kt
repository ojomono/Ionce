package com.ojomono.ionce.firebase

// TODO: Avoid Android imports and move to separated module when needed for more UI platforms
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
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
    private var userRegistration: ListenerRegistration? = null

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
     * Get the tale document with id=[id].
     */
    fun getTale(id: String): Task<DocumentSnapshot> =
        userDocRef?.collection(CP_TALES)?.document(id)?.get()
            ?.addOnSuccessListener { document ->
                if (document != null) Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                else Log.d(TAG, "No such document")
            }?.addOnFailureListener { exception -> Log.d(TAG, "get failed with ", exception) }
            ?: throw Utils.NoSignedInUserException

    /**
     * Save [tale] to database, overwriting existing document of creating new. Return a [Task]
     * holding the success state and the new tale's document reference.
     */
    fun setTale(tale: TaleModel) = userDocRef?.let { userRef ->

        // Get reference for wanted tale - if does not exist create new reference
        val talesCol = userRef.collection(CP_TALES)
        val taleRef = if (tale.id.isEmpty()) talesCol.document() else talesCol.document(tale.id)

        // Set the tale item in the tales list (add or overwrite)
        val tales = getUpdatedTalesList(taleRef.id) {
            val taleItem = TaleItemModel(tale, taleRef.id)
            if (it == -1) add(taleItem) else this[it] = taleItem
        }

        // Set the tale's document and update the user's tale list
        runTaleBatch(taleRef.id, tales) { set(taleRef, tale) }

    } ?: throw Utils.NoSignedInUserException

    /**
     * Delete the tale document with id=[id].
     */
    fun deleteTale(id: String): Task<DocumentReference> {

        // Remove the tale from the tales list
        val tales = getUpdatedTalesList(id) { if (it != -1) removeAt(it) }

        // Delete the tale document and update the user's tale list
        return runTaleBatch(id, tales) { delete(it) }
    }

    /**
     * Add [mediaUri] to media list of tale [id], in given [index].
     */
    fun addMediaToTale(id: String, mediaUri: String, index: Int) =

        // Get the tale document
        getTale(id).continueWithTask { getTask ->
            if (!getTask.isSuccessful) Tasks.forCanceled()
            else getTask.result?.toObject(TaleModel::class.java)?.let { tale ->

                // Add media at given index
                val updatedMedia = tale.media.apply { add(index, mediaUri) }

                // Update cover for tale in the tales list (if needs update)
                val tales =
                    if (index != 0) null
                    else getUpdatedTalesList(id) { set(it, get(it).copy(cover = tale.media[0])) }

                // Update the tale's media list and the user's tale list
                runTaleBatch(id, tales) { update(it, TaleModel::media.name, updatedMedia) }
            }
        }

    /**
     * Remove all media of tale [id].
     */
    fun removeTaleMedia(id: String): Task<DocumentReference> {

        // Remove cover for tale in the tales list
        val tales = getUpdatedTalesList(id) { set(it, get(it).copy(cover = "")) }

        // Update the tale's media list and the user's tale list
        return runTaleBatch(id, tales) { update(it, TaleModel::media.name, emptyList<String>()) }
    }

    // For drag n' drop feature
//    /**
//     * Update the current users tales list so it holds the current tales order.
//     */
//    fun saveTalesOrder() =
//        userDocRef?.update(FN_TALES, userTales.value)
//            ?.addOnSuccessListener { Log.d(TAG, "userDocRef successfully updated!") }
//            ?.addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }

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
            userDocRef?.addSnapshotListener { snapshot, e ->
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
     * Get the current user's tales list after the given [update] was made on the item with [id].
     */
    private fun getUpdatedTalesList(id: String, update: MutableList<TaleItemModel>.(Int) -> Unit) =
        userTales.value?.apply { update(indexOfFirst { item -> item.id == id }) }
            ?: throw Utils.NoSignedInUserException

    /**
     * In a batch write, run the given [action] on the document with [id] and update the current
     * user's tale list to [tales] if given.
     */
    private fun runTaleBatch(
        id: String,
        tales: List<TaleItemModel>?,
        action: WriteBatch.(DocumentReference) -> WriteBatch
    ) = userDocRef?.let { userRef ->

        // Create reference for wanted tale to use inside batch
        val taleRef = userRef.collection(CP_TALES).document(id)

        // In a batch write: update user's tale list, and run given action on the tale ref
        db.runBatch {
            if (tales != null) it.update(userRef, UserModel::tales.name, tales)
            it.action(taleRef)
        }
            .addOnSuccessListener { Log.d(TAG, "Batch write success!") }
            .addOnFailureListener { e -> Log.w(TAG, "Batch write failure.", e) }

            // Return a task holding the success state and the tale's document reference
            .continueWithTask { task ->
                if (task.isSuccessful) Tasks.forResult(taleRef) else Tasks.forCanceled()
            }
    } ?: throw Utils.NoSignedInUserException
}