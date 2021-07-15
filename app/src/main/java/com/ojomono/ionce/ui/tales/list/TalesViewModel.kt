package com.ojomono.ionce.ui.tales.list

import androidx.lifecycle.LiveData
import com.ojomono.ionce.firebase.repositories.TaleRepository
import com.ojomono.ionce.firebase.Storage
import com.ojomono.ionce.firebase.Utils
import com.ojomono.ionce.models.TaleItemModel
import com.ojomono.ionce.models.TaleModel
import com.ojomono.ionce.utils.BaseViewModel
import com.ojomono.ionce.utils.continueIfSuccessful

class TalesViewModel : BaseViewModel(), TalesAdapter.TalesListener {
    // The user's tales list    // TODO: Use a Repository class
    val tales: LiveData<MutableList<TaleItemModel>> = TaleRepository.userTales

    // The tale currently being deleted
    private var clickedTale: TaleItemModel? = null

    // Types of supported events
    sealed class EventType : BaseEventType() {
        class ShowEditTaleDialog(val taleId: String) : EventType()
        class ShowDeleteTaleDialog(val taleTitle: String) : EventType()
    }

    // For drag n' drop feature
//    // Did the user change the tales order?
//    private var wasOrderChanged = false
//
//    override fun onMoved(fromPosition: Int, toPosition: Int) {
//        // Move the tale to it's right place in the LiveData list
//        tales.value?.let {
//            if (fromPosition < toPosition) {
//                for (i in fromPosition until toPosition) Collections.swap(it, i, i + 1)
//            } else {
//                for (i in fromPosition downTo toPosition + 1) Collections.swap(it, i, i - 1)
//            }
//        }
//        wasOrderChanged = true
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        if (wasOrderChanged) TaleRepository.saveTalesOrder()?.withProgressBar()
//    }

    /************************/
    /** post event methods **/
    /************************/

    fun onAdd() = postEvent(EventType.ShowEditTaleDialog(""))

    override fun onEdit(taleItem: TaleItemModel) =
        postEvent(EventType.ShowEditTaleDialog(taleItem.id))

    override fun onDelete(taleItem: TaleItemModel) {
        clickedTale = taleItem
        postEvent(EventType.ShowDeleteTaleDialog(taleItem.title))
    }

    /*******************/
    /** logic methods **/
    /*******************/

    fun clearClickedTale() = run { clickedTale = null }

    fun deleteTale() = clickedTale?.let { taleItem ->

        // Cancel all active upload tasks for current tale
        Storage.getActiveTaleTasks(taleItem.id).forEach { it.cancel() }

        // Get the tale model
        TaleRepository.getTale(taleItem.id).continueIfSuccessful { getTask ->
            getTask.result?.toObject(TaleModel::class.java)?.let { tale ->

                // Delete the tale's media
                val storageTask = Storage.deleteFiles(tale.media)

                // Delete tale document
                Utils.continueWithTaskOrInNew(storageTask, true) {
                    TaleRepository.deleteTale(tale.id)
                }
            }
        }

        clearClickedTale()  // Not waiting for callback to support offline mode
    }
}