package com.ojomono.ionce.ui.tales.edit

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.firebase.Storage
import com.ojomono.ionce.models.TaleModel
import com.ojomono.ionce.utils.EventStateHolder
import com.ojomono.ionce.utils.ImageUtils.UPLOADING_IN_PROGRESS

class EditTaleViewModel(private val taleId: String = "") : ViewModel() {
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
     * Save the tale to the database. In no change made, return null
     */
    fun saveTale(): Task<Void>? =
        tale.value?.let { taleModel ->

            // First, save tale model to database (if differs from old one)
            // Notice media is not yet saved - this initial save is to refresh the tales list fast
            if (didTaleChange()) Database.setTale(taleModel).continueWithTask { task ->

                // If initial save succeed - save cover, using id from result document (might be new
                // generated id, in the case of a new tale). If no save needed for cover, return a
                // successful task.
                if (task.isSuccessful)
                    task.result?.id?.let { saveCover(taleModel, it) ?: Tasks.forResult(null) }

                // If initial save failed - return an unsuccessful task
                else Tasks.forCanceled()

                // If no change was made to the tale model - just save the cover (if needed)
            } else saveCover(taleModel)
        }

    /**
     * Check if any changes were made.
     */
    fun didTaleChange() = run { (tale.value != taleCopy) or didCoverChange() }

    /**
     * Update tale cover to given [uri].
     */
    fun updateDisplayedCover(uri: Uri) {
        _cover.value = uri
        _tale.value?.media = listOf(UPLOADING_IN_PROGRESS)
    }

    /*********************/
    /** private methods **/
    /*********************/

    /**
     * Init the tale model with the tale from the database, or with a new empty tale. Anyway, save a
     * copy to allow check if any changes were made.
     */
    private fun initTale() {

        // If id is empty, init empty tale (and save a copy to allow check if any changes were made)
        if (taleId.isEmpty()) fillFields(TaleModel())

        // Else, get tale from database
        else Database.getTale(taleId)
            .addOnSuccessListener { documentSnapshot ->
                documentSnapshot.toObject(TaleModel::class.java)?.let {

                    // Save a copy to allow check if any changes were made
                    fillFields(it)

                    // If the cover is currently uploading, listen for changes to refresh it
                    if (it.media.firstOrNull() == UPLOADING_IN_PROGRESS)
                        Database.registerToTale(taleId) { refreshedTale ->

                            // If cover is not uploading anymore, refresh and remove registration
                            if (refreshedTale.media.firstOrNull() == UPLOADING_IN_PROGRESS) false
                            else {
                                fillFields(refreshedTale)
                                true
                            }
                        }
                }
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
     * Check if the tale cover has changed.
     */
    private fun didCoverChange() = run { cover.value?.toString() != taleCopy.media.firstOrNull() }

    /**
     * Upload the new cover to Storage, save it's link in the [taleModel], update document with
     * given [id] and delete old cover from Storage. If user didn't change cover, return null.
     */
    private fun saveCover(taleModel: TaleModel, id: String = ""): Task<Void>? =

        // If cover didn't change - do nothing.
        if (!didCoverChange()) null
        else {
            val oldCover = taleCopy.media.firstOrNull()

            // id is used for the case of consecutive calls to setTale:
            // one before this function, for a fast db update, and one in here that updates media
            // list. If the first call created a new tale, we want to update the same one and not
            // create a new one, so we need the generated id
            val savedTale = if (taleModel.id.isNotEmpty()) taleModel else taleModel.copy(id = id)

            // If new cover is not null, upload the new cover to Storage
            cover.value?.toString()?.let { newCover ->
                Storage.uploadTaleCover(savedTale.id, Uri.parse(newCover))

                    // Now handle database document:
                    .continueWithTask { uploadTask ->

                        // If Storage upload failed - return failed task (and leave document as is)
                        if (!uploadTask.isSuccessful or (uploadTask.result == null))
                            Tasks.forCanceled()

                        // If Storage upload succeed, replace url in document to the new url
                        else {
                            // In this case, uploadedCover != null
                            val uploadedCover = uploadTask.result.toString()
                            savedTale.media = listOf(uploadedCover)
                            Database.setTale(savedTale)

                                // Now handle orphan file in Storage:
                                .continueWithTask { setTask ->

                                    // If database update failed - delete new cover from Storage
                                    if (!setTask.isSuccessful) deleteIfNotNull(uploadedCover)

                                    // If database update succeed, and new cover has different path
                                    // than the old one - delete old cover from Storage
                                    else if (oldCover != uploadedCover) deleteIfNotNull(oldCover)

                                    // If new cover has same path as old one (should never happen as
                                    // name is randomly generated, but just in case:) return a
                                    // successful task.
                                    else Tasks.forResult(null)
                                }
                        }
                    }

                // If new cover is null, just delete the old one
            } ?: deleteIfNotNull(oldCover)

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

    /**
     * If [file] is not null, delete it, else return a dummy successful [Task].
     */
    private fun deleteIfNotNull(file: String?): Task<Void> =
        file?.let { Storage.deleteFile(it) } ?: Tasks.forResult(null)

}