package com.ojomono.ionce.ui.tales.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.ojomono.ionce.R
import com.ojomono.ionce.firebase.repositories.TaleRepository
import com.ojomono.ionce.firebase.Storage
import com.ojomono.ionce.firebase.Utils
import com.ojomono.ionce.models.TaleItemModel
import com.ojomono.ionce.models.TaleModel
import com.ojomono.ionce.ui.bases.BaseViewModel
import com.ojomono.ionce.utils.continueIfSuccessful

class TalesViewModel : BaseViewModel(), TalesListAdapter.TalesListener {
    // The user's tales list
    private val userTales = TaleRepository.userTales
    private val heardTales = TaleRepository.heardTales

    // The currently shown tales list (determined by the toggle)
    private val _shownList =
        MutableLiveData<Int>().apply {
            userTales.observeForever { postValue(value) }
            heardTales.observeForever { postValue(value) }
        }
    val shownList: LiveData<Int> = _shownList
    val shownTales = Transformations.map(shownList) {
        when (it) {
            R.id.button_my_tales -> userTales
            R.id.button_heard_tales -> heardTales
            else -> userTales
        }.value
    }

    // Observe the tales lists in order to refresh the shown list for any change
    private val listsObserver =
        Observer<MutableList<TaleItemModel>?> { _shownList.postValue(_shownList.value) }
            .also { userTales.observeForever(it) }.also { heardTales.observeForever(it) }

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

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCleared() {
        super.onCleared()
        userTales.removeObserver(listsObserver)
        heardTales.removeObserver(listsObserver)
    }

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

    fun setShownList(checkedId: Int) = _shownList.postValue(checkedId)
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