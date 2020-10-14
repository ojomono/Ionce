package com.ojomono.ionce.ui.tales

import android.net.Uri
import androidx.lifecycle.LiveData
import com.ojomono.ionce.firebase.Authentication
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.models.TaleItemModel
import com.ojomono.ionce.utils.BaseViewModel

class TalesViewModel : BaseViewModel(), TalesAdapter.TalesListener {
    // The user's tales list
    val tales: LiveData<List<TaleItemModel>> = Database.userTales

    // Types of supported events
    sealed class EventType() : Event {
        object ShowAddTaleDialog : EventType()
        class ShowEditTaleDialog(val taleItem: TaleItemModel) : EventType()
        class ShowDeleteTaleDialog(val id: String) : EventType()
    }

    /************************/
    /** post event methods **/
    /************************/

    fun onAdd() = postEvent(EventType.ShowAddTaleDialog)
    override fun onEdit(taleItem: TaleItemModel) = postEvent(EventType.ShowEditTaleDialog(taleItem))
    override fun onDelete(id: String) = postEvent(EventType.ShowDeleteTaleDialog(id))

    /*******************/
    /** logic methods **/
    /*******************/

    fun addTale(title: String) = Database.createTale(title)
    fun updateTale(taleItem: TaleItemModel) = Database.updateTale(taleItem)
    fun deleteTale(id: String) = Database.deleteTale(id)

}