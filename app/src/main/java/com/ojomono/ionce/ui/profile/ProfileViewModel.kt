package com.ojomono.ionce.ui.profile

import android.net.Uri
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseUser    // TODO avoid importing firebase packages here
import com.ojomono.ionce.R
import com.ojomono.ionce.firebase.Authentication
import com.ojomono.ionce.utils.BaseViewModel

class ProfileViewModel : BaseViewModel(), PopupMenu.OnMenuItemClickListener {

    /************/
    /** Fields **/
    /************/

    // Current logged in user
    val user: LiveData<FirebaseUser?> = Authentication.currentUser

    // Refresh the user data (in case the name/photo/... was changed on another device)
    init {
        Authentication.reloadCurrentUser()
    }

    // Types of supported events
    sealed class EventTypes() : Event {
        class ShowPopupMenu(val view: View) : Event
        object ShowImagePicker : Event
        object ShowEditNameDialog : Event
    }

    /************************/
    /** post event methods **/
    /************************/

    // TODO onChangePhoto should open picture activity to view photo. Actions should be there.
    fun onSettingsClicked(view: View) = postEvent(EventTypes.ShowPopupMenu(view))
    fun onPictureClicked() = postEvent(EventTypes.ShowImagePicker)
    fun onNameClicked() = postEvent(EventTypes.ShowEditNameDialog)

    /**********************************************/
    /** MenuItem.OnMenuItemClickListener methods **/
    /**********************************************/

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_sign_out -> {
                Authentication.signOut()
                true
            }
            else -> false
        }
    }

    /*******************/
    /** logic methods **/
    /*******************/

    fun updateUserPicture(uri: Uri) = Authentication.updatePhotoUrl(uri, true)
    fun updateUserName(name: String) = Authentication.updateDisplayName(name)

}