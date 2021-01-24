package com.ojomono.ionce.ui.tales.edit

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.firebase.Storage
import com.ojomono.ionce.models.TaleModel
import com.ojomono.ionce.utils.EventStateHolder

class EditTaleViewModel(private var taleId: String = "") : ViewModel() {
    // The tale currently being edited
    private val _tale: MutableLiveData<TaleModel> = MutableLiveData<TaleModel>()
    val tale: LiveData<TaleModel> = _tale

    // A copy of the initial tale data to allow checking if any changes were made
    private lateinit var taleCopy: TaleModel

    // The displayed cover (separate LiveData to allow refresh without refreshing all)
    private val _cover: MutableLiveData<Uri> = MutableLiveData<Uri>()
    val cover: LiveData<Uri> = _cover

    // Init all LiveData fields
    init {
        initTale()
    }

    // The event listener
    val events = EventStateHolder()

    // Types of supported events
    sealed class EventType : EventStateHolder.Event {
        object ShowImagePicker : EventType()
        object GetCompressedCover : EventType()
    }

    /**********************/
    /** on click methods **/
    /**********************/

    fun onPictureClicked() = events.postEvent(EventType.ShowImagePicker)

    fun onCoverClear() = run { _cover.value = null }

    /*******************/
    /** logic methods **/
    /*******************/

    /**
     * Check if any changes were made.
     */
    fun didTaleChange() = run { didModelChange() or didCoverChange() }

    /**
     * Update tale cover to given [uri].
     */
    fun updateDisplayedCover(uri: Uri) = run { _cover.value = uri }

    /**
     * Save the tale to the database. In no change made, return null.
     */
    fun saveTale(): Task<DocumentReference>? =
        tale.value?.let { taleModel ->

            // First, save tale model to database (if differs from old one)
            // Notice media is not yet saved - this initial save is to refresh the tales list fast
            if (didModelChange())
                Database.setTale(taleModel).addOnSuccessListener {

                    // If database succeed - get the document id (in case it is a new generated
                    // document and we don't know it yet) and save the cover (if changed)
                    taleId = it.id
                    if (didCoverChange()) events.postEvent(EventType.GetCompressedCover)
                }
            else {

                // If no changes were made to the tale model, just save the cover (if changed)
                if (didCoverChange()) events.postEvent(EventType.GetCompressedCover)
                null
            }
        }

    /**
     * Upload the new cover [bitmap] to Storage, save it's link in the current tale's model,
     * update it's document (use [taleId] in case it's a new document) and delete old
     * cover from Storage. If user didn't change cover, return null.
     */
    fun saveCover(bitmap: ByteArray?): Task<Void>? =
        tale.value?.let { taleModel ->

            // If cover didn't change - do nothing.
            if (!didCoverChange()) null
            else {
                val oldCoverUri = taleModel.media.firstOrNull()

                // taleId is used for the case of consecutive calls to setTale:
                // one before this function, for a fast db update, and one in here that updates
                // media list. If the first call created a new tale, we want to update the same one
                // and not create a new one, so we need the generated id
                val savedTale =
                    if (taleModel.id.isNotEmpty()) taleModel else taleModel.copy(id = taleId)

                // If new cover is not null, upload the new cover to Storage
                bitmap?.let { newCoverBitmap ->
                    Storage.uploadTaleCover(savedTale.id, newCoverBitmap)

                        // Now handle database document:
                        .continueWithTask { uploadTask ->

                            // If Storage upload failed - return failed task (and leave document as
                            // is)
                            if (!uploadTask.isSuccessful or (uploadTask.result == null))
                                Tasks.forCanceled()

                            // If Storage upload succeed, replace url in document to the new url
                            else {
                                // In this case, uploadedCover != null
                                val uploadedCoverUri = uploadTask.result.toString()
                                savedTale.media = listOf(uploadedCoverUri)
                                Database.setTale(savedTale)

                                    // Now handle orphan file in Storage:
                                    .continueWithTask { setTask ->

                                        // If database update failed - delete new cover from Storage
                                        if (!setTask.isSuccessful) deleteIfNotNull(uploadedCoverUri)

                                        // If database update succeed, and new cover has different
                                        // path than the old one - delete old cover from Storage
                                        else if (oldCoverUri != uploadedCoverUri)
                                            deleteIfNotNull(oldCoverUri)

                                        // If new cover has same path as old one (should never
                                        // happen as name is randomly generated, but just in case:)
                                        // return a successful task.
                                        else Tasks.forResult(null)
                                    }
                            }
                        }

                    // If new cover is null, just delete the old one
                } ?: deleteIfNotNull(oldCoverUri)

                    // Now handle database document:
                    .continueWithTask { deleteTask ->

                        // If Storage delete failed - return failed task
                        if (!deleteTask.isSuccessful) deleteTask

                        // If Storage delete succeed - remove link from tale document
                        else {
                            savedTale.media = listOf()
                            Database.setTale(savedTale)
                                .continueWithTask { setTask ->
                                    if (setTask.isSuccessful) Tasks.forResult(null)
                                    else Tasks.forCanceled()
                                }
                        }
                    }
            }
        }

    /*********************/
    /** private methods **/
    /*********************/

    /**
     * Init the tale model with the tale from the database, or with a new empty tale. Anyway, save a
     * copy to allow check if any changes were made.
     */
    private fun initTale() {

        // If id is empty - init empty tale (and save a copy to allow check if any changes were made)
        if (taleId.isEmpty()) fillFields(TaleModel())

        // Else, get tale from database (and save a copy to allow check if any changes were made)
        else Database.getTale(taleId)
            .addOnSuccessListener { documentSnapshot ->
                documentSnapshot.toObject(TaleModel::class.java)?.let { fillFields(it) }
            }
    }

    /**
     * Fill all LiveData fields with the right data from the given [tale].
     */
    private fun fillFields(tale: TaleModel) {
        _tale.value = tale
        taleCopy = tale.copy()
        _cover.value = tale.media.firstOrNull()?.let { Uri.parse(it) }
    }

    /**
     * Check if the tale model has changed.
     */
    private fun didModelChange() = run { tale.value != taleCopy }

    /**
     * Check if the tale cover has changed.
     */
    private fun didCoverChange() = run { cover.value?.toString() != taleCopy.media.firstOrNull() }

    /**
     * If [file] is not null, delete it, else return a dummy successful [Task].
     */
    private fun deleteIfNotNull(file: String?): Task<Void> =
        file?.let { Storage.deleteFile(it) } ?: Tasks.forResult(null)
}