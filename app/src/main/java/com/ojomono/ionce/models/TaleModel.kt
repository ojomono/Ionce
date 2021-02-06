package com.ojomono.ionce.models

import com.google.firebase.firestore.DocumentId

/**
 * Data model to hold all the tale's model
 * (saved in the 'tales' sub-collection with generated [id]).
 */
data class TaleModel(
    @DocumentId val id: String = "",
    var title: String = "",
    val media: MutableList<String> = mutableListOf()
)