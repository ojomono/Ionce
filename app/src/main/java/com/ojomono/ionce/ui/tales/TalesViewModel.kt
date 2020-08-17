package com.ojomono.ionce.ui.tales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.models.TalesItem
import com.ojomono.ionce.utils.OneTimeEvent

class TalesViewModel : ViewModel(), TalesAdapter.TalesListener {
    val tales: LiveData<List<TalesItem>> = Database.userTales

    // One time event for the fragment to listen to
    private val _itemEvent = MutableLiveData<OneTimeEvent<Any>>()
    val itemEvent: LiveData<OneTimeEvent<Any>> = _itemEvent

    // Types of the supported events
    class AddItemEvent() : OneTimeEvent<Unit>(Unit)
    class UpdateItemEvent(item: TalesItem) : OneTimeEvent<TalesItem>(item)
    class DeleteItemEvent(item: TalesItem) : OneTimeEvent<TalesItem>(item)

    fun onAdd() {
        _itemEvent.value = AddItemEvent()
    }

    /****************************************/
    /** TalesAdapter.TalesListener methods **/
    /****************************************/

    override fun onEdit(item: TalesItem) {
        _itemEvent.value = UpdateItemEvent(item)
    }

    override fun onDelete(item: TalesItem) {
        _itemEvent.value = DeleteItemEvent(item)
    }

    /**
     * Create a new tale document with the given [title].
     */
    fun addTale(title: String) {
        Database.setTale(title = title)
    }

    /**
     * Update the tale document which id's = [id] to have the given [title].
     */
    fun updateTale(id: String, title: String) {
        Database.setTale(id, title)
    }

    /**
     * Delete the tale document which id's = [id].
     */
    fun deleteTale(id: String) {
        Database.deleteTale(id)
    }
}