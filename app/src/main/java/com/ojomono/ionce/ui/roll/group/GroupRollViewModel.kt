package com.ojomono.ionce.ui.roll.group

import androidx.lifecycle.Transformations
import com.ojomono.ionce.firebase.repositories.GroupRepository
import com.ojomono.ionce.models.UserItemModel
import com.ojomono.ionce.ui.bases.BaseViewModel

class GroupRollViewModel : BaseViewModel(), UsersListAdapter.UsersListener {
    // The current group of the user
    val group = GroupRepository.model
    val members = Transformations.map(group) { it?.members?.values?.toList() }

    // Types of supported events
    sealed class EventType : BaseEventType() {
        object OpenQRCodeScanner : EventType()
        class ShowUserTalesDialog(val uid: String) : EventType()
    }

    /**********************/
    /** on click methods **/
    /**********************/

    fun onJoinClicked() = postEvent(EventType.OpenQRCodeScanner)
    fun onCreateClicked() = GroupRepository.createGroup()
    fun onLeaveClicked() = GroupRepository.leaveGroup()
    override fun onTales(userItem: UserItemModel) =
        postEvent(EventType.ShowUserTalesDialog(userItem.id))

    /*******************/
    /** logic methods **/
    /*******************/

    fun joinGroup(groupId: String) = GroupRepository.joinGroup(groupId)

}