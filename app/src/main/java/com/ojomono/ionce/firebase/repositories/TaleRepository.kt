package com.ojomono.ionce.firebase.repositories

// TODO: Avoid Android imports and move to separated module when needed for more UI platforms
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.firebase.Utils
import com.ojomono.ionce.models.*
import com.ojomono.ionce.utils.TAG

object TaleRepository {

    /***************/
    /** Constants **/
    /***************/

    // Collection Paths
    private const val CP_TALES = "tales"

    /************/
    /** Fields **/
    /************/

    // TODO replace liveData with callbackFlow / StateFlow when they become non-experimental
    //  https://medium.com/firebase-tips-tricks/how-to-use-kotlin-flows-with-firestore-6c7ee9ae12f3

    // Current user's tales list
    val userTales: LiveData<MutableList<TaleItemModel>> =
        Transformations.map(Database.userData) { it?.tales }

    /********************/
    /** public methods **/
    /********************/

    /**
     * Get the tale document with id=[id].
     */
    fun getTale(id: String): Task<DocumentSnapshot> =
        Database.userDocRef?.collection(CP_TALES)?.document(id)?.get()
            ?.addOnSuccessListener { document ->
                if (document != null) Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                else Log.d(TAG, "No such document")
            }?.addOnFailureListener { exception -> Log.d(TAG, "get failed with ", exception) }
            ?: throw Utils.NoSignedInUserException

    /**
     * Save [tale] to database, overwriting existing document of creating new. Return a [Task]
     * holding the success state and the new tale's document reference.
     */
    fun setTale(tale: TaleModel) =
        Database.userDocRef?.let { userRef ->

            // Get reference for wanted tale - if does not exist create new reference
            val talesCol = userRef.collection(CP_TALES)
            val taleRef = if (tale.id.isEmpty()) talesCol.document() else talesCol.document(tale.id)

            // Set the tale item in the tales list (add or overwrite)
            val tales = buildUpdatedTalesList(taleRef.id) {
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
        val tales = buildUpdatedTalesList(id) { if (it != -1) removeAt(it) }

        // Delete the tale document and update the user's tale list
        return runTaleBatch(id, tales) { delete(it) }
    }

    /**
     * Update the tale document with id=[id] to have the given [media] list.
     */
    fun updateTaleMedia(id: String, media: List<String>): Task<DocumentReference> {

        // Update media for tale in the tales list
        val tales =
            buildUpdatedTalesList(id) { set(it, get(it).copy(cover = media.firstOrNull() ?: "")) }

        // Update the tale's media list and the user's tale list
        return runTaleBatch(id, tales) { update(it, TaleModel::media.name, media) }
    }

    /**
     * Update the tale document with id=[id] to have the given [coverUri].
     */
    fun updateTaleCover(id: String, coverUri: String?) =
        updateTaleMedia(id, coverUri?.let { listOf(it) } ?: emptyList())

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
     * Get the current user's tales list after the given [update] was made on the item with [id].
     */
    private fun buildUpdatedTalesList(
        id: String,
        update: MutableList<TaleItemModel>.(Int) -> Unit
    ) =
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
    ) = Database.userDocRef?.let { userRef ->

        // Create reference for wanted tale to use inside batch
        val taleRef = userRef.collection(CP_TALES).document(id)

        // In a batch write: update user's tale list, and run given action on the tale ref
        Database.runBatch {
            if (tales != null) it.update(userRef, UserModel::tales.name, tales)
            it.action(taleRef)
        }.continueWithTask { task ->
            // Return a task holding the success state and the tale's document reference
            if (task.isSuccessful) Tasks.forResult(taleRef) else Tasks.forCanceled()
        }
    } ?: throw Utils.NoSignedInUserException
}