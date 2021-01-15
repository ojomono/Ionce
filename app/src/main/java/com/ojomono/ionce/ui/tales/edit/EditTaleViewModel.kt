package com.ojomono.ionce.ui.tales.edit

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.ojomono.ionce.firebase.Authentication
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.firebase.Storage
import com.ojomono.ionce.models.TaleModel
import com.ojomono.ionce.utils.EventStateHolder
import com.ojomono.ionce.utils.TAG
import com.ojomono.ionce.utils.Utils

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

    /*******************/
    /** logic methods **/
    /*******************/

    /**
     * Save the tale to the database.
     */
    fun saveTale(): Task<Void>? =
        tale.value?.let { taleModel ->

            // If displayed cover differs then the one current in the model, the user changed cover:
            // upload the new one to Storage
            val oldCover = taleModel.media.firstOrNull()?.let { uri -> Uri.parse(uri) }
            val uploadCoverTask = cover.value?.let { newCover ->
                if (newCover != oldCover) Storage.uploadTaleCover(taleModel.id, newCover)
                else null
            }

            // Define and return the saving task (on top of the cover upload task if happened)
            Utils.continueWithTaskOrInNew(uploadCoverTask) { getCoverUrlTask ->

                // If cover uploaded successfully, replace url in the tale model to the new url
                var uploadedCover: Uri? = null
                getCoverUrlTask?.let {
                    if (getCoverUrlTask.isSuccessful) {
                        uploadedCover = getCoverUrlTask.result
                        taleModel.media = listOf(uploadedCover.toString())
                    } else Log.e(TAG, getCoverUrlTask.exception.toString())
                }

                // Save tale model to database (if differs from old one)
                if (didTaleChange())
                    Database.setTale(taleModel)
                        ?.continueWithTask { setTaleTask ->

                            // If a new cover was uploaded, the "orphan" cover (the old or new one,
                            // depending if the db save succeed) should be deleted from Storage
                            uploadedCover?.let { newCover ->

                                // If the db save was successful - delete old cover file
                                if (setTaleTask.isSuccessful)
                                    oldCover?.let { Storage.deleteFile(it) }

                                // If the db save failed - delete new cover file
                                else Storage.deleteFile(newCover)
                            }
                        } else null
            }
        }

    /**
     * Check if any changes were made.
     */
    fun didTaleChange() = run { tale.value != taleCopy }

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

        // If id is empty - init empty tale (and save a copy to allow check if any changes were made)
        if (taleId.isEmpty()) fillFields(TaleModel())

        // Else, get tale from database (and save a copy to allow check if any changes were made)
        else Database.getTale(taleId)?.addOnSuccessListener { documentSnapshot ->
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

}