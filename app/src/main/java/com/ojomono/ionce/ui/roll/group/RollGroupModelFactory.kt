package com.ojomono.ionce.ui.roll.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Taken from: https://developer.android.com/codelabs/kotlin-android-training-quality-and-states#3
class RollGroupModelFactory(private val groupId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RollGroupViewModel::class.java)) {
            return RollGroupViewModel(groupId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}