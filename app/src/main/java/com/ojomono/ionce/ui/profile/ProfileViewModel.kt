package com.ojomono.ionce.ui.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.ojomono.ionce.firebase.Authentication
import com.ojomono.ionce.utils.OneTimeEvent

class ProfileViewModel : ViewModel() {
    // Text to be shown
    private val _text = MutableLiveData<String>().apply {
        value = Authentication.getCurrentUser()?.email
    }
    val text: LiveData<String> = _text

    // One time event for the fragment to listen to
    private val _event =
        MutableLiveData<OneTimeEvent<(Context, OnCompleteListener<Void>) -> Unit>>()
    val event: LiveData<OneTimeEvent<(Context, OnCompleteListener<Void>) -> Unit>> = _event

    /**
     * Sign out to user.
     */
    fun onSignOut() {
        _event.value = OneTimeEvent(Authentication::signOut)
    }
}