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

    fun createTale(title: String) {
        TODO("Not yet implemented")
    }

    fun updateTale(id: Long, title: String) {
        TODO("Not yet implemented")
    }

    fun deleteTale(id: Long) {
        TODO("Not yet implemented")
    }

}