package com.ojomono.ionce.ui.roll

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ojomono.ionce.R
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.models.TaleItemData
import com.ojomono.ionce.utils.OneTimeEvent

class RollViewModel : ViewModel() {
    // The user's tales list
    val tales: LiveData<List<TaleItemData>> = Database.userTales

    // The rolled tale's title
    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text

    // One time event for the fragment to listen to in order to show error texts
    private val _error = MutableLiveData<OneTimeEvent<Int>>()
    val error: LiveData<OneTimeEvent<Int>> = _error

    /**
     * Show a random tale title from the user's tales.
     */
    fun onRoll() {
        // If the user have no tales yet - show him an error toast
        if (tales.value.isNullOrEmpty())
            _error.value = OneTimeEvent(R.string.roll_error_no_tales)
        // If he has some - get a random one and show it's title
        else _text.value = tales.value?.random()?.title
    }
}