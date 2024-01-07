package com.ojomono.ionce.ui.tales.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Taken from: https://developer.android.com/codelabs/kotlin-android-training-quality-and-states#3
class EditTaleModelFactory(private val taleId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditTaleViewModel::class.java)) {
            return EditTaleViewModel(taleId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}