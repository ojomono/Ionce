package com.ojomono.ionce.ui.tales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.models.TalesItem
import com.ojomono.ionce.utils.OneTimeEvent

class TalesViewModel : ViewModel(), TalesAdapter.TalesListener {
    // The user's tales list
    val tales: LiveData<List<TalesItem>> = Database.userTales

    // Types of supported events
    sealed class EventType(val onOk: (item: TalesItem) -> Unit) {
        class AddItemEvent() : EventType(Database::setTale)
        class UpdateItemEvent(val item: TalesItem) : EventType(Database::setTale)
        class DeleteItemEvent(val item: TalesItem) : EventType(Database::deleteTale)
    }

    // One time event for the fragment to listen to
    private val _itemEvent = MutableLiveData<OneTimeEvent<EventType>>()
    val itemEvent: LiveData<OneTimeEvent<EventType>> = _itemEvent

    /**
     * Show dialog for new tale creation.
     */
    fun onAdd() {
        _itemEvent.value = OneTimeEvent(EventType.AddItemEvent())
    }

    /****************************************/
    /** TalesAdapter.TalesListener methods **/
    /****************************************/

    /**
     * Show dialog for tale title update.
     */
    override fun onEdit(item: TalesItem) {
        _itemEvent.value = OneTimeEvent(EventType.UpdateItemEvent(item))
    }

    /**
     * Show dialog for tale deletion.
     */
    override fun onDelete(item: TalesItem) {
        _itemEvent.value = OneTimeEvent(EventType.DeleteItemEvent(item))
    }
}