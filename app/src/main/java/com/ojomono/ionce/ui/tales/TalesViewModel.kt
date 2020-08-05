package com.ojomono.ionce.ui.tales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.models.Tale

class TalesViewModel : ViewModel() {
    private val _tales = MutableLiveData<List<Tale>>().apply {
        value = Database.userTales
    }
    val tales: LiveData<List<Tale>> = _tales

    /**
     * Create a new tale document with the given [title].
     */
    fun addTale(title: String) {
        Database.addTale(title)
    }

    fun updateTale(id: String, title: String) {
        TODO("Not yet implemented")
    }

    fun deleteTale(id: String) {
        TODO("Not yet implemented")
    }
}