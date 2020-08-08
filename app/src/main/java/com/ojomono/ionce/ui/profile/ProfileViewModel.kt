package com.ojomono.ionce.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ojomono.ionce.firebase.Authentication

class ProfileViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = Authentication.getCurrentUser()?.email
    }
    val text: LiveData<String> = _text
}