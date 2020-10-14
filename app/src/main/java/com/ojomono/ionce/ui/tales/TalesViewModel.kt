package com.ojomono.ionce.ui.tales

import androidx.lifecycle.LiveData
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.models.TaleItemModel
import com.ojomono.ionce.utils.BaseViewModel

class TalesViewModel : BaseViewModel(), TalesAdapter.TalesListener {
    // The user's tales list
    val tales: LiveData<List<TaleItemModel>> = Database.userTales

    // The tale currently being edited or deleted
    private var clickedTale: TaleItemModel? = null

    // Types of supported events
    sealed class EventType() : Event {
        class ShowEditTaleDialog(val taleTitle: String) : EventType()
        class ShowDeleteTaleDialog(val taleTitle: String) : EventType()
        object ShowAddTaleDialog : EventType()
    }

    /************************/
    /** post event methods **/
    /************************/

    fun onAdd() = postEvent(EventType.ShowAddTaleDialog)

    override fun onEdit(taleItem: TaleItemModel) {
        clickedTale = taleItem
        postEvent(EventType.ShowEditTaleDialog(taleItem.title))
    }

    override fun onDelete(taleItem: TaleItemModel) {
        clickedTale = taleItem
        postEvent(EventType.ShowDeleteTaleDialog(taleItem.title))
    }

    /*******************/
    /** logic methods **/
    /*******************/

    fun clearClickedTale() {
        clickedTale = null
    }

    fun addTale(title: String) = Database.createTale(title)

    fun updateTale(title: String) =
    // Copy is needed because if we change the original item, adapter's new list and old list would
        // be the same and it will not refresh. Thus a copy is needed.
        clickedTale?.copy(title = title)
            ?.let { Database.updateTale(it)?.addOnCompleteListener { clearClickedTale() } }

    fun deleteTale() =
        clickedTale
            ?.let { Database.deleteTale(it.id)?.addOnCompleteListener { clearClickedTale() } }
}