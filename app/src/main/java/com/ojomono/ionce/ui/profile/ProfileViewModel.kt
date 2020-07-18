package com.ojomono.ionce.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ojomono.ionce.utils.FirebaseProxy

class ProfileViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = FirebaseProxy.getCurrentUser()?.email
    }
    val text: LiveData<String> = _text
}