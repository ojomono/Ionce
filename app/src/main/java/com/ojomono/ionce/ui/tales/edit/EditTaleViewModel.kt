package com.ojomono.ionce.ui.tales.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.models.TaleModel

class EditTaleViewModel(private val taleId: String = "") : ViewModel() {
    // The tale currently being edited
    private val _tale: MutableLiveData<TaleModel> =
        MutableLiveData<TaleModel>().apply { initTale() }
    val tale: LiveData<TaleModel> = _tale

    // A copy of the initial tale data to allow checking if any changes were made
    private lateinit var taleCopy: TaleModel

    /**
     * Save the tale to the database.
     */
    fun saveTale() = if (taleChanged()) tale.value?.let { Database.setTale(it) } else null

    /**
     * Check if any changes were made.
     */
    fun taleChanged() = run { tale.value != taleCopy }

    /**
     * Init the tale model with the tale from the database, or with a new empty tale. Anyway, save a
     * copy to allow check if any changes were made.
     */
    private fun MutableLiveData<TaleModel>.initTale() {

        // If id is empty - init empty tale (and save a copy to allow check if any changes were made)
        if (taleId.isEmpty()) value = TaleModel().also { taleCopy = it.copy() }

        // Else, get tale from database (and save a copy to allow check if any changes were made)
        else Database.getTale(taleId)?.addOnSuccessListener { documentSnapshot ->
            value = documentSnapshot.toObject(TaleModel::class.java)?.also { taleCopy = it.copy() }
        }
    }
}