package com.ojomono.ionce.ui.tales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ojomono.ionce.firebase.Authentication
import com.ojomono.ionce.firebase.Database

class TalesViewModel : ViewModel() {

    private val _tales = MutableLiveData<List<TaleItemModel>>().apply {
        value = Database.getUserTales()
    }
    val tales: LiveData<List<TaleItemModel>> = _tales
}