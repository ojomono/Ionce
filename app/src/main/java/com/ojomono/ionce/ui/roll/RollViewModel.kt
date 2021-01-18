package com.ojomono.ionce.ui.roll

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ojomono.ionce.R
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.models.TaleItemModel
import com.ojomono.ionce.utils.BaseViewModel

class RollViewModel : BaseViewModel() {
    // The user's tales list    // TODO: Use a Repository class
    val tales: LiveData<MutableList<TaleItemModel>> = Database.userTales

    // The rolled tale's title
    private val _rolled = MutableLiveData<TaleItemModel>()
    val rolled: LiveData<TaleItemModel> = _rolled

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
            else filter { it.id != rolled.value?.id }.random()
        }
}