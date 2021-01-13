package com.ojomono.ionce.ui.tales.edit

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.models.TaleModel
import com.ojomono.ionce.utils.EventStateHolder

class EditTaleViewModel(private val taleId: String = "") : ViewModel() {
    // The tale currently being edited
    private val _tale: MutableLiveData<TaleModel> = MutableLiveData<TaleModel>()
    val tale: LiveData<TaleModel> = _tale

    // A copy of the initial tale data to allow checking if any changes were made
    private lateinit var taleCopy: TaleModel

    // The presented tale cover (separate LiveData to allow refresh without refreshing all)
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
    fun saveTale() = if (didTaleChange()) tale.value?.let { Database.setTale(it) } else null

    /**
     * Check if any changes were made.
     */
    fun didTaleChange() = run { tale.value != taleCopy }

    /**
     * Update tale cover to given [uri].
     */
    fun updateTaleCover(uri: Uri) = run { _cover.value = uri }

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
        _cover.value = tale.media.firstOrNull()
    }

}