package com.ojomono.ionce.ui.profile

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser    // TODO avoid importing firebase packages here
import com.ojomono.ionce.firebase.Authentication
import com.ojomono.ionce.utils.OneTimeEvent

class ProfileViewModel : ViewModel() {

    /************/
    /** Fields **/
    /************/

    // Current logged in user
    val user: LiveData<FirebaseUser?> = Authentication.currentUser

    // Refresh the user data (in case the name/photo/... was changed on another device)
    init {
        Authentication.reloadCurrentUser()
    }

    // One time event for the fragment to listen to
    private val _event = MutableLiveData<OneTimeEvent<EventType<Nothing>>>()
    val event: LiveData<OneTimeEvent<EventType<Nothing>>> = _event

    // Types of supported events
    sealed class EventType<in T>(val func: (T) -> Task<Void>?) {
        class ShowMenuEvent(func: (Context) -> Task<Void>?, val view: View) :
            EventType<Context>(func)

        class ChangePhotoEvent(func: (Uri) -> Task<Void>?) : EventType<Uri>(func)
        class EditNameEvent(func: (String) -> Task<Void>?) : EventType<String>(func)
        class EditEmailEvent(func: (String) -> Task<Void>?) : EventType<String>(func)
        class SignOutEvent(func: (Context) -> Task<Void>?) : EventType<Context>(func)
    }

    /*************************/
    /** raise event methods **/
    /*************************/

    /**
     * Show popup menu.
     */
    fun onShowMenu(view: View) {
        _event.value = OneTimeEvent(EventType.ShowMenuEvent(Authentication::signOut, view))
    }

    /**
     * Change user's profile picture.
     */
    fun onChangePhoto() {
        // TODO open picture activity to view photo. Actions should be there.
        _event.value = OneTimeEvent(EventType.ChangePhotoEvent(Authentication::updatePhotoUrl))
    }

    /**
     * Edit the user's display name.
     */
    fun onEditName() {
        _event.value = OneTimeEvent(EventType.EditNameEvent(Authentication::updateDisplayName))
    }

    /**
     * Edit the user's email.
     */
    fun onEditEmail() {
        _event.value = OneTimeEvent(EventType.EditEmailEvent(Authentication::updateEmail))
    }

    /**
     * Sign out the user.
     */
    fun onSignOut() {
        _event.value = OneTimeEvent(EventType.SignOutEvent(Authentication::signOut))
    }

}