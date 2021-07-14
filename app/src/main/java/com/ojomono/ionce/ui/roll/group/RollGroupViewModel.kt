package com.ojomono.ionce.ui.roll.group

import com.ojomono.ionce.utils.BaseViewModel

class RollGroupViewModel(private var groupId: String = "") : BaseViewModel() {

    // Types of supported events
    sealed class EventType : BaseEventType() {
    }

}