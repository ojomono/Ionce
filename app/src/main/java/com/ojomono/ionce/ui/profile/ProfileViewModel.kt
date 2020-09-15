package com.ojomono.ionce.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser    // TODO avoid importing firebase packages here
import com.ojomono.ionce.firebase.Authentication
import com.ojomono.ionce.utils.OneTimeEvent

class ProfileViewModel : ViewModel() {
    // Current logged in user
    val user: LiveData<FirebaseUser?> = Authentication.currentUser

    // One time event for the fragment to listen to
    private val _event = MutableLiveData<OneTimeEvent<EventType>>()
    val event: LiveData<OneTimeEvent<EventType>> = _event

    // Types of supported events
    sealed class EventType() {
        class SignOutEvent(val func: (Context, OnCompleteListener<Void>) -> Unit) : EventType()
        class EditNameEvent(val func: (String) -> Unit) : EventType()
        class ChangePhotoEvent(val func: (Uri?) -> Unit) : EventType()
    }

    /*************************/
    /** raise event methods **/
    /*************************/

    /**
     * Sign out the user.
     */
    fun onSignOut() {
        _event.value = OneTimeEvent(EventType.SignOutEvent(Authentication::signOut))
    }

    /**
     * Edit the user's display name.
     */
    fun onEditName() {
        _event.value = OneTimeEvent(EventType.EditNameEvent(Authentication::updateDisplayName))
    }

    /**
     * Change user's profile picture.
     */
    fun onChangePhoto() {
        _event.value = OneTimeEvent(EventType.ChangePhotoEvent(Authentication::updatePhotoUri))
    }

}