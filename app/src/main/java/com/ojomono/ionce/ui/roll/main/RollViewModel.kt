package com.ojomono.ionce.ui.roll.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.ojomono.ionce.R
import com.ojomono.ionce.firebase.repositories.TaleRepository
import com.ojomono.ionce.models.TaleItemModel
import com.ojomono.ionce.utils.BaseViewModel

class RollViewModel : BaseViewModel() {
    // The user's tales list    // TODO: Use a Repository class
    val tales: LiveData<MutableList<TaleItemModel>> = TaleRepository.userTales

    // The rolled tale's title
    private val _rolled = MutableLiveData<TaleItemModel>()
    val rolled: LiveData<TaleItemModel> = _rolled

    // Observe tales list in case the rolled tale cover finished upload, and refresh screen
    private val listObserver =
        Observer<MutableList<TaleItemModel>> { list ->
            list.find { it.id == rolled.value?.id }
                ?.let { if (rolled.value != it) _rolled.value = it }
        }.also { tales.observeForever(it) }

    // Types of supported events
    sealed class EventType : BaseEventType() {
        class ShowRollGroupDialog(val groupId: String) : EventType()
    }

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCleared() {
        super.onCleared()
        tales.removeObserver(listObserver)
    }

    /************************/
    /** post event methods **/
    /************************/

    fun onGroup() = postEvent(EventType.ShowRollGroupDialog(""))

    /*******************/
    /** logic methods **/
    /*******************/

    /**
     * Show a random tale title from the user's tales.
     */
    fun onRoll() {
        // If the user has no tales yet - show him an error toast
        if (tales.value.isNullOrEmpty())
            showMessageByResId(R.string.roll_error_no_tales)
        // If he has some - get a random one and show it's title
        else _rolled.value = getRandomTale()
    }

    /**
     * Get a random tale, excluding the last rolled one (unless user has only one tale).
     */
    private fun getRandomTale(): TaleItemModel? =
        tales.value?.run {
            // If the user has only one tale - get it
            if (size == 1) get(0)
            // If he has more, get a random one, excluding the last rolled tale
            // TODO use "minusElement"
            else filter { it.id != rolled.value?.id }.random()
        }
}