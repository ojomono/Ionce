package com.ojomono.ionce.ui.roll.group

import com.ojomono.ionce.firebase.repositories.GroupRepository
import com.ojomono.ionce.utils.bases.BaseViewModel

class GroupRollViewModel() : BaseViewModel() {
    // The current group of the user
    val group = GroupRepository.userGroup

    // Types of supported events
    sealed class EventType : BaseEventType() {
    }

    /**********************/
    /** on click methods **/
    /**********************/

    fun onCreateGroup() = GroupRepository.createGroup()

}