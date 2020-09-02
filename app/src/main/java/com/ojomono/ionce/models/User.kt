package com.ojomono.ionce.models

import com.google.firebase.firestore.DocumentId

/**
 * Data model to hold all the user's data (saved in the 'users' collection with [id] = user's uid
 * from firebase authentication)
 */
data class User(
    @DocumentId val id: String = "",
    val tales: MutableList<TaleItemData> = mutableListOf()
) {
    /**
     * Add or overwrite a tale from the list.
     */
    fun setTale(taleItem: TaleItemData) {
        // If given taleItem is in the list (searched by id) - overwrite it with new data, else add it
        val index = tales.indexOfFirst { it.id == taleItem.id }
        if (index == -1) tales.add(taleItem)
        else tales[index] = taleItem
    }

    /**
     * Delete a tale from the list.
     */
    fun deleteTale(id: String) {
        // If given id is in the list - delete it
        val index = tales.indexOfFirst { it.id == id }
        if (index != -1) tales.removeAt(index)
    }
}