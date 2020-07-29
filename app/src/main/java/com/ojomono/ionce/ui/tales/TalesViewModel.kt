package com.ojomono.ionce.ui.tales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ojomono.ionce.firebase.Database

class TalesViewModel : ViewModel() {
    private val _tales = MutableLiveData<List<Tale>>().apply {
        value = Database.getUserTales()
    }
    val tales: LiveData<List<Tale>> = _tales

    /**
     * Create a new tale document with the given [title].
     */
    fun addTale(title: String) {
        Database.addTale(title)
    }

    fun updateTale(id: Long, title: String) {
        TODO("Not yet implemented")
    }

    fun deleteTale(id: Long) {
        TODO("Not yet implemented")
    }

}