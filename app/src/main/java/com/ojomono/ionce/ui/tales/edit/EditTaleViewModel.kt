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

    /**
     * Save the tale to the database.
     */
    fun saveTale() = tale.value?.let { Database.setTale(it) }

    /**
     * Init the tale model with the tale from the DB, or with a new empty tale.
     */
    private fun MutableLiveData<TaleModel>.initTale() {
        if (taleId.isEmpty()) value = TaleModel()
        else Database.getTale(taleId)?.addOnSuccessListener { documentSnapshot ->
            value = documentSnapshot.toObject(TaleModel::class.java)
        }
    }
}