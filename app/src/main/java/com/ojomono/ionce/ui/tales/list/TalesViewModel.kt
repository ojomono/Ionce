package com.ojomono.ionce.ui.tales.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.map
import com.ojomono.ionce.firebase.repositories.TaleRepository
import com.ojomono.ionce.firebase.Storage
import com.ojomono.ionce.firebase.Utils
import com.ojomono.ionce.models.TaleItemModel
import com.ojomono.ionce.models.TaleModel
import com.ojomono.ionce.ui.bases.BaseViewModel
import com.ojomono.ionce.utils.continueIfSuccessful

class TalesViewModel : BaseViewModel(), TalesListAdapter.TalesListener {

    /*************/
    /** Classes **/
    /*************/

    // The tales lists
    enum class ListType { MY_TALES, HEARD_TALES }

    // Types of supported events
    sealed class EventType : BaseEventType() {
        class ShowEditTaleDialog(val taleId: String) : EventType()
        class ShowDeleteTaleDialog(val taleTitle: String) : EventType()
    }

    /**************/
    /** LiveData **/
    /**************/

    // The user's tales list
    private val userTales = TaleRepository.userTales
    private val heardTales = TaleRepository.heardTales

    // The currently shown tales list (determined by the toggle)
    private val _shownList =
        MutableLiveData<ListType>().apply {
            userTales.observeForever { postValue(value) }
            heardTales.observeForever { postValue(value) }
        }
    val shownList: LiveData<ListType> = _shownList
    val shownTales = shownList.map {
        when (it) {
            ListType.MY_TALES -> userTales
            ListType.HEARD_TALES -> heardTales
        }.value
    }

    /***************/
    /** Observers **/
    /***************/

    // Observe the tales lists in order to refresh the shown list for any change
    private val listsObserver =
        Observer<MutableList<TaleItemModel>?> { _shownList.postValue(_shownList.value) }
            .also { userTales.observeForever(it) }.also { heardTales.observeForever(it) }

    // The tale currently being deleted
    private var clickedTale: TaleItemModel? = null

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

    fun setShownList(checkedList: ListType) = _shownList.postValue(checkedList)
    fun clearClickedTale() = run { clickedTale = null }
    fun deleteTale() = clickedTale?.let { taleItem ->
        if (shownList.value == ListType.MY_TALES) deleteUserTale(taleItem)
        else TaleRepository.removeFriendTale(taleItem)
        clearClickedTale()  // Not waiting for callback to support offline mode
    }

    /**
     * Delete a tale that belongs to the current user.
     */
    private fun deleteUserTale(taleItem: TaleItemModel) {

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
    }
}