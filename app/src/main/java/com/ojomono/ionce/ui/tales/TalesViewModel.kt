package com.ojomono.ionce.ui.tales

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.models.TalesItem

class TalesViewModel : ViewModel() {
    val tales: LiveData<List<TalesItem>> = Database.userTales

    /**
     * Create a new tale document with the given [title].
     */
    fun addTale(title: String) {
        Database.setTale(title = title)
    }

    /**
     * Update the tale document which id's = [id] to have the given [title].
     */
    fun updateTale(id: String, title: String) {
        Database.setTale(id, title)
    }

    /**
     * Delete the tale document which id's = [id].
     */
    fun deleteTale(id: String) {
        Database.deleteTale(id)
    }
}