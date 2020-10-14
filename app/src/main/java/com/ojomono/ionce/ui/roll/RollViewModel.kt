package com.ojomono.ionce.ui.roll

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ojomono.ionce.R
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.models.TaleItemModel
import com.ojomono.ionce.utils.BaseViewModel

class RollViewModel : BaseViewModel() {
    // The user's tales list
    val tales: LiveData<List<TaleItemModel>> = Database.userTales

    // The rolled tale's title
    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text

    // Types of supported events
    sealed class EventType() : Event {
        class ShowErrorMessage(val messageResId: Int) : EventType()
    }

    /**
     * Show a random tale title from the user's tales.
     */
    fun onRoll() {
        // If the user have no tales yet - show him an error toast
        if (tales.value.isNullOrEmpty())
            postEvent(EventType.ShowErrorMessage(R.string.roll_error_no_tales))
        // If he has some - get a random one and show it's title
        else _text.value = tales.value?.random()?.title
    }
}