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
    fun addTale(talesItem: TalesItem) {
        talesItems.add(talesItem)
    }
}