package com.ojomono.ionce.firebase.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.models.BaseModel
import com.ojomono.ionce.utils.TAG

abstract class DocHoldingRepo<T : BaseModel>(
    private val modelClass: Class<T>,
    private val collectionPath: String
) {

    // TODO replace liveData with callbackFlow / StateFlow when they become non-experimental
    //  https://medium.com/firebase-tips-tricks/how-to-use-kotlin-flows-with-firestore-6c7ee9ae12f3

    // Always keep current document loaded
    private val document = MutableLiveData<DocumentSnapshot?>()
    private var registration: ListenerRegistration? = null

    // Public access to structured data
    val model = Transformations.map(document) { it?.toObject(modelClass) }
    val docRef: DocumentReference?
        get() = model.value?.id?.let { Database.collection(collectionPath).document(it) }

    /**
     * Switch the current document snapshot to those of the given [id] - and listen to changes.
     * Return the get [Task].
     */
    protected open fun switchDocument(id: String): Task<DocumentSnapshot>? {

        var task: Task<DocumentSnapshot>? = null

        // If no id was given - clear document
        if (id.isEmpty()) {
            registration?.remove()
            document.value = null

            // If the new id belongs to another document then the one we currently refer to
        } else if ((model.value?.id ?: "") != id) {

            // If a change listener is registered to the previous document - remove it
            registration?.remove()

            // Get the current document reference
            val newDocRef = Database.collection(collectionPath).document(id)

            // Save the document locally
            task = newDocRef.get()
            task.addOnSuccessListener { document.value = it }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }

            // Listen for changes in the document
            registration = newDocRef.addSnapshotListener { snapshot, e ->
                if (e != null) Log.w(TAG, "Listen failed.", e)
                else document.value = snapshot
            }
        }

        return task
    }

    /**
     * Reload the current document data.
     */
    fun reloadDocument() =
        docRef?.get()?.addOnSuccessListener { document.value = it }
            ?.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

}