package com.ojomono.ionce.ui.profile

import android.net.Uri
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseUser    // TODO avoid importing firebase packages here
import com.google.firebase.auth.UserInfo        // TODO avoid importing firebase packages here
import com.ojomono.ionce.R
import com.ojomono.ionce.firebase.Authentication
import com.ojomono.ionce.utils.BaseViewModel

class ProfileViewModel : BaseViewModel(), PopupMenu.OnMenuItemClickListener {
    // Current logged in user
    val user: LiveData<FirebaseUser?> = Authentication.currentUser  // TODO: Use a Repository class

    // User info of the different providers                         // TODO: Use a Repository class
    val googleUserInfo: LiveData<UserInfo> = Authentication.googleUserInfo
    val facebookUserInfo: LiveData<UserInfo> = Authentication.facebookUserInfo
    val twitterUserInfo: LiveData<UserInfo> = Authentication.twitterUserInfo

    // Types of supported events
    sealed class EventType() : Event {
        class ShowPopupMenu(val view: View) : EventType()
        object ShowImagePicker : EventType()
        object ShowEditNameDialog : EventType()
        object ShowLinkWithTwitter : EventType()
    }

    /********************/
    /** Initialization **/
    /********************/

    // Refresh the user data (in case the name/photo/... was changed on another device)
    init {
        refresh()
    }

    /**********************/
    /** on click methods **/
    /**********************/

    // TODO onChangePhoto should open picture activity to view photo. Actions should be there.
    fun onSettingsClicked(view: View) = postEvent(EventType.ShowPopupMenu(view))
    fun onPictureClicked() = postEvent(EventType.ShowImagePicker)
    fun onNameClicked() = postEvent(EventType.ShowEditNameDialog)
    fun onTwitterClicked() = postEvent(EventType.ShowLinkWithTwitter)

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

    fun refresh() = Authentication.reloadCurrentUser()
    fun updateUserPicture(uri: Uri) = Authentication.updatePhotoUrl(uri, true)
    fun updateUserName(name: String) = Authentication.updateDisplayName(name)

}