package com.ojomono.ionce.ui.roll.group

import androidx.lifecycle.map
import com.ojomono.ionce.firebase.repositories.GroupRepository
import com.ojomono.ionce.firebase.repositories.UserRepository
import com.ojomono.ionce.models.TaleItemModel
import com.ojomono.ionce.models.UserItemModel
import com.ojomono.ionce.ui.bases.BaseViewModel

class GroupRollViewModel : BaseViewModel(), UsersListAdapter.UsersListener {
    // The current group of the user
    val group = GroupRepository.model
    val members = group.map { it?.members?.values?.toList() }
    val currentUser = UserRepository.model

    // Types of supported events
    sealed class EventType : BaseEventType() {
        object OpenQRCodeScanner : EventType()
        class ShowUserTalesDialog(val user: UserItemModel) : EventType()
    }

    /**********************/
    /** on click methods **/
    /**********************/

    // Reloading user because for some reason when the group is created by a new user, the joining
    // user's screen doesn't refresh after scanning. For now, this solves it.
    fun onRefreshClicked() =
        UserRepository.reloadDocument()?.continueWithTask { GroupRepository.reloadDocument() }

    fun onJoinClicked() = postEvent(EventType.OpenQRCodeScanner)
    fun onCreateClicked() = GroupRepository.createGroup()
    fun onLeaveClicked() = GroupRepository.leaveGroup()
    override fun onTales(userItem: UserItemModel) =
        postEvent(EventType.ShowUserTalesDialog(userItem))

    /*******************/
    /** logic methods **/
    /*******************/

    fun joinGroup(groupId: String) = GroupRepository.joinGroup(groupId)

    fun setFriendTales(uid: String, tales: List<TaleItemModel>) =
        members.value?.find { it.id == uid }?.let {
            UserRepository.setFriend(
                UserItemModel(uid, it.displayName, tales as MutableList<TaleItemModel>)
            )
        }

}