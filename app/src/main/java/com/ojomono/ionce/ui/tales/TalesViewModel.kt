package com.ojomono.ionce.ui.tales

import androidx.lifecycle.LiveData
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.models.TaleItemModel
import com.ojomono.ionce.utils.BaseViewModel
import java.util.*

class TalesViewModel : BaseViewModel(), TalesAdapter.TalesListener {
    // The user's tales list    // TODO: Use a Repository class
    val tales: LiveData<MutableList<TaleItemModel>> = Database.userTales

    // The tale currently being edited or deleted
    private var clickedTale: TaleItemModel? = null

    // Did the user change the tales order?
    private var wasOrderChanged = false

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

    fun addTale(title: String) = Database.createTale(title)?.withProgressBar()

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        // Move the tale to it's right place in the LiveData list
        tales.value?.let {
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) Collections.swap(it, i, i + 1)
            } else {
                for (i in fromPosition downTo toPosition + 1) Collections.swap(it, i, i - 1)
            }
        }
        wasOrderChanged = true
    }

    fun updateTale(title: String) =
    // Copy is needed because if we change the original item, adapter's new list and old list would
        // be the same and it will not refresh. Thus a copy is needed.
        clickedTale?.copy(title = title)
            ?.let {
                Database.updateTale(it)?.withProgressBar()
                    ?.addOnCompleteListener { clearClickedTale() }
            }

    fun deleteTale() =
        clickedTale
            ?.let {
                Database.deleteTale(it.id)?.withProgressBar()
                    ?.addOnCompleteListener { clearClickedTale() }
            }

    override fun onCleared() {
        super.onCleared()
        if (wasOrderChanged) Database.saveTalesOrder()?.withProgressBar()
    }
}