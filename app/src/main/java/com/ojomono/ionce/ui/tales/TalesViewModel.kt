package com.ojomono.ionce.ui.tales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.Transaction
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.models.TaleItemModel
import com.ojomono.ionce.utils.OneTimeEvent

class TalesViewModel : ViewModel(), TalesAdapter.TalesListener {
    // The user's tales list
    val tales: LiveData<List<TaleItemModel>> = Database.userTales

    // One time event for the fragment to listen to
    private val _itemEvent = MutableLiveData<OneTimeEvent<EventType>>()
    val itemEvent: LiveData<OneTimeEvent<EventType>> = _itemEvent

    // Types of supported events
    sealed class EventType(val onOk: (taleItem: TaleItemModel) -> Task<Transaction>?) {
        class AddItemEvent() : EventType(Database::setTale)
        class UpdateItemEvent(val taleItem: TaleItemModel) : EventType(Database::setTale)
        class DeleteItemEvent(val taleItem: TaleItemModel) : EventType(Database::deleteTale)
    }

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
    override fun onEdit(taleItem: TaleItemModel) {
        _itemEvent.value = OneTimeEvent(EventType.UpdateItemEvent(taleItem))
    }

    /**
     * Show dialog for tale deletion.
     */
    override fun onDelete(taleItem: TaleItemModel) {
        _itemEvent.value = OneTimeEvent(EventType.DeleteItemEvent(taleItem))
    }
}