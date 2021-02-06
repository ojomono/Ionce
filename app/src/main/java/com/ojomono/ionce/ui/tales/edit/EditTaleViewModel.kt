package com.ojomono.ionce.ui.tales.edit

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Tasks
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.firebase.Storage
import com.ojomono.ionce.models.TaleModel
import com.ojomono.ionce.utils.EventStateHolder

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
     * Save the tale to the database. If no change made, return null
     */
    fun saveTale() = tale.value?.let { taleModel ->

        // If no change was made to the tale model - just save the cover (if needed)
        if (!didTaleChange()) saveCover(taleModel.id)

        // If there were changes in the tale model, save it to database
        // Notice media is not yet saved - this initial save is to refresh the tales list fast
        else Database.setTale(taleModel).continueWithTask { modelTask ->

            // If initial save failed - return an the failed task
            if (!modelTask.isSuccessful) modelTask

            // If initial save succeed - save cover, using id from result document (might be new
            // generated id, in the case of a new tale).
            else modelTask.result?.id?.let {
                saveCover(it)

                    // Convert result to Task<DocumentReference>
                    ?.continueWithTask { coverTask ->
                        if (!coverTask.isSuccessful) Tasks.forCanceled()
                        else modelTask

                        // If no save needed for cover, just return the successful initial task.
                    } ?: modelTask
            }
        }
    }

    /**
     * Check if any changes were made.
     */
    fun didTaleChange() = run { (tale.value != taleCopy) or didCoverChange() }

    /**
     * Update tale cover to given [uri].
     */
    fun updateDisplayedCover(uri: Uri) = run { _cover.value = uri }

    /*********************/
    /** private methods **/
    /*********************/

    /**
     * Init the tale model with the tale from the database, or with a new empty tale. Anyway, save a
     * copy to allow check if any changes were made.
     */
    private fun initTale() {

        // If id is empty, init empty tale
        if (taleId.isEmpty()) fillFields(TaleModel())

        // Else, get tale from database
        else Database.getTale(taleId)
            .addOnSuccessListener { documentSnapshot ->
                documentSnapshot.toObject(TaleModel::class.java)?.let { fillFields(it) }
            }
    }

    /**
     * Fill all LiveData fields with the right data from the given [tale].
     */
    private fun fillFields(tale: TaleModel) {
        _tale.value = tale      // Put in LiveData
        taleCopy = tale.copy()  // Save copy to check if changes were made
        _cover.value = tale.media.firstOrNull()?.let { Uri.parse(it) }
    }

    /**
     * Check if the tale cover has changed.
     */
    private fun didCoverChange() = run { cover.value?.toString() != taleCopy.media.firstOrNull() }

    /**
     * Save displayed cover to current [taleId] document and Storage.
     */
    private fun saveCover(taleId: String) =
        if (!didCoverChange()) null // If cover didn't change do nothing
        // TODO if both are not null, try to replace existing file in storage
        else
            deleteTaleMedia(taleId)
                .continueWithTask { cover.value?.let { addTaleCover(taleId, it) } }

    /**
     * Add [image] as [taleId] cover.
     */
    private fun addTaleCover(taleId: String, image: Uri) =

        // Upload new image to Storage
        Storage.uploadTaleMedia(taleId, image).continueWithTask { uploadTask ->
            if (!uploadTask.isSuccessful) Tasks.forCanceled()
            else {

                // If upload succeed - add uploaded uri to tale media in db
                val uploadedUri = uploadTask.result.toString()
                Database.addMediaToTale(taleId, uploadedUri, 0).continueWithTask { addTask ->

                    // If db update succeed return the task, else try to revert Storage upload
                    if (addTask.isSuccessful) addTask
                    else {
                        // TODO if fails we have an "orphan" file in storage - remember uri
                        Storage.deleteFile(uploadedUri)
                        Tasks.forCanceled()
                    }
                }
            }
        }

    /**
     * Remove all [taleId]'s media.
     */
    private fun deleteTaleMedia(taleId: String) =

        // Get the tale model
        Database.getTale(taleId).continueWithTask { getTask ->
            if (!getTask.isSuccessful) Tasks.forCanceled()
            else getTask.result?.toObject(TaleModel::class.java)?.let { tale ->

                // Delete the tale's media
                Storage.deleteFiles(tale.media)?.continueWithTask {
                    if (!it.isSuccessful) Tasks.forCanceled()

                    // Remove media from document
                    else Database.removeTaleMedia(taleId)
                }
            }
        }
}