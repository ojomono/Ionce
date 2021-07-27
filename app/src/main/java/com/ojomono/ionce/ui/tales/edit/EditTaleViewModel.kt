package com.ojomono.ionce.ui.tales.edit

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.ojomono.ionce.firebase.repositories.TaleRepository
import com.ojomono.ionce.firebase.Storage
import com.ojomono.ionce.models.TaleModel
import com.ojomono.ionce.ui.bases.BaseViewModel
import com.ojomono.ionce.utils.addFallbackTask
import com.ojomono.ionce.utils.continueIfSuccessful

class EditTaleViewModel(private var taleId: String = "") : BaseViewModel() {
    // The tale currently being edited
    private val _tale: MutableLiveData<TaleModel> = MutableLiveData<TaleModel>()
    val tale: LiveData<TaleModel> = _tale

    // A copy of the initial tale data to allow checking if any changes were made
    private lateinit var taleCopy: TaleModel

    // The displayed cover (separate LiveData to allow refresh without refreshing all)
    private val _cover: MutableLiveData<Uri?> = MutableLiveData<Uri?>()
    val cover: LiveData<Uri?> = _cover

    // Init all LiveData fields
    init {
        initTale()
    }

    // Types of supported events
    sealed class EventType : BaseEventType() {
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
        if (!didTaleChange()) saveCover()

        // If there were changes in the tale model, save it to database
        // Notice media is not yet saved - this initial save is to refresh the tales list fast
        else TaleRepository.setTale(taleModel).continueWithTask { modelTask ->

            // If initial save failed - return an the failed task
            if (!modelTask.isSuccessful) modelTask

            // If initial save succeed - save cover, using id from result document (might be new
            // generated id, in the case of a new tale).
            else modelTask.result?.id?.let {
                taleId = it // Save document id in ViewModel member for use inside the class
                saveCover()

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

        // Els it is an existing tale
        else {
            // Get the tale from TaleRepository
            TaleRepository.getTale(taleId)
                .addOnSuccessListener { documentSnapshot ->
                    documentSnapshot.toObject(TaleModel::class.java)?.let { fillFields(it) }
                }

            // Get the active Storage tasks for the tale
            for (task in Storage.getActiveTaleTasks(taleId)) {
                task.continueWithTask { task.result.storage.downloadUrl }
                    .addOnSuccessListener {

                        // If no new cover has been chosen, update the displayed cover
                        if (!didCoverChange()) updateDisplayedCover(it)

                        // Anyway, put the uploaded cover uri in the tale media for change check
                        _tale.value?.media = listOf(it.toString())
                    }
            }
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
    private fun didCoverChange() =
        run { cover.value?.toString() != _tale.value?.media?.firstOrNull() }

    /**
     * Save displayed cover to current [taleId] document and Storage.
     */
    private fun saveCover() =

        // Cover didn't change (old = new) - do nothing
        if (!didCoverChange()) null
        else {

            // Cancel all active upload tasks for current tale
            Storage.getActiveTaleTasks(taleId).forEach { it.cancel() }

            // Get the up-to-date tale document before starting any changes
            TaleRepository.getTale(taleId).continueIfSuccessful { getTask ->

                val taleMedia = getTask.result?.toObject(TaleModel::class.java)?.media

//                // If tale has more than one media, that means a previous cover failed to delete:
//                // Try to delete all irrelevant media
//                taleMedia?.let {
//                    if (it.size > 1) deleteTask = Storage.deleteFiles(it.subList(1, it.size))
//                }

                cover.value?.let { new ->

                    // Case 1: Both old & new covers are not null - UPDATE cover
                    taleMedia?.firstOrNull()?.let { old -> updateCover(old, new) }

                    // Case 2: Old cover is null - ADD the new one
                        ?: addCover(new)

                    // Case 3: New cover is null - REMOVE the old one
                } ?: taleMedia?.firstOrNull()?.let { old -> removeCover(old) }
            }
        }

    private fun addCover(cover: Uri) =

        // Upload new cover to Storage
        Storage.uploadTaleMedia(taleId, cover).continueIfSuccessful { uploadTask ->
            uploadTask.result?.let { uploadedUri ->

                // Update cover url in TaleRepository
                TaleRepository.updateTaleCover(taleId, uploadedUri.toString())

                    // If update fails - try to revert upload
                    .addFallbackTask { Storage.deleteFile(uploadedUri.toString()) }
            }
        }

    private fun updateCover(old: String, new: Uri) =

        // Upload new cover to Storage
        Storage.uploadTaleMedia(taleId, new).continueIfSuccessful { uploadTask ->
            uploadTask.result?.toString()?.let { uploadedUri ->

                // Update cover url in TaleRepository
                TaleRepository.updateTaleCover(taleId, uploadedUri)

                    // If update fails - try to revert upload
                    .addFallbackTask { Storage.deleteFile(uploadedUri) }

                    // Delete old cover from storage
                    .continueIfSuccessful {
                        Storage.deleteFile(old)

                            // If delete fails - try to put both in media list
                            .addFallbackTask {
                                TaleRepository.updateTaleMedia(taleId, listOf(uploadedUri, old))
                            }
                    }
            }
        }

    private fun removeCover(cover: String) =

        // Delete old cover from storage
        Storage.deleteFile(cover).continueIfSuccessful {

            // Update cover url in TaleRepository
            TaleRepository.updateTaleCover(taleId, null)

        }
}