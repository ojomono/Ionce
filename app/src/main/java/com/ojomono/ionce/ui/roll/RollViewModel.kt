package com.ojomono.ionce.ui.roll

import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ojomono.ionce.R
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.models.TalesItem

class RollViewModel : ViewModel() {
    val tales: LiveData<List<TalesItem>> = Database.userTales
    val hintResourceId = ObservableInt()

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text

    // Set the hint text
    init {
        setHintText()
    }

    /**
     * Check if the [tales] list is empty, and set the hint accordingly
     * ("hit 'roll!'" / "no tales found")
     */
    fun setHintText() {
        val stringName =
            if (tales.value.isNullOrEmpty()) R.string.roll_hint_no_tales_text
            else R.string.roll_hint_default_text
        if (stringName != hintResourceId.get()) hintResourceId.set(stringName)
    }

    fun onRoll() {

    }
}