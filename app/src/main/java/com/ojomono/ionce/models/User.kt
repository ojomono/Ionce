package com.ojomono.ionce.models

import com.google.firebase.firestore.DocumentId

/**
 * Data model to hold all the user's data (saved in the 'users' collection with [id] = user's uid
 * from firebase authentication)
 */
data class User(
    @DocumentId val id: String = "",
    val talesItems: MutableList<TalesItem> = mutableListOf()
) {
    // TODO: Maybe replace talesItems 'list' with 'set'
    //  (more logical but maybe harder to use with Firestore or RecyclerView)

    /**
     * Add or overwrite a tale from the list.
     */
    fun setTale(talesItem: TalesItem) {
        // If given item is in the list (searched by id) - overwrite it with new data, else add it
        val index = talesItems.indexOfFirst { it.id == talesItem.id }
        if (index == -1) talesItems.add(talesItem)
        else talesItems[index] = talesItem
    }

    /**
     * Delete a tale from the list.
     */
    fun deleteTale(id: String) {
        // If given id is in the list - delete it
        val index = talesItems.indexOfFirst { it.id == id }
        if (index != -1) talesItems.removeAt(index)
    }
}