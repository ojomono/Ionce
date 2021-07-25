package com.ojomono.ionce.ui.roll.group

import com.ojomono.ionce.firebase.repositories.GroupRepository
import com.ojomono.ionce.utils.bases.BaseViewModel

class GroupRollViewModel : BaseViewModel() {
    // The current group of the user
    val group = GroupRepository.group

    // Types of supported events
    sealed class EventType : BaseEventType() {
        object OpenQRCodeScanner : EventType()
    }

    /**********************/
    /** on click methods **/
    /**********************/

    fun onJoinClicked() = postEvent(EventType.OpenQRCodeScanner)
    fun onCreateClicked() = GroupRepository.createGroup()

    /*******************/
    /** logic methods **/
    /*******************/

    fun joinGroup(groupId: String) = GroupRepository.joinGroup(groupId)
}