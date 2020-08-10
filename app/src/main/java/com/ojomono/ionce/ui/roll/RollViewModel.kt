package com.ojomono.ionce.ui.roll

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.models.TalesItem

class RollViewModel : ViewModel() {
    val tales: LiveData<List<TalesItem>> = Database.userTales

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text

    /**
     * Show a random tale title from the user's tales.
     */
    fun onRoll() {
        if (tales.value.isNullOrEmpty()) TODO("tell fragment")
        else _text.value = tales.value?.random()?.title
    }
}