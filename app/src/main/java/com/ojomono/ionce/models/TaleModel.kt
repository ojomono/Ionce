package com.ojomono.ionce.models

import android.net.Uri
import com.google.firebase.firestore.DocumentId

/**
 * Data model to hold all the tale's model
 * (saved in the 'tales' sub-collection with generated [id]).
 */
data class TaleModel(
    @DocumentId val id: String = "",
    var title: String = "",
    val media: MutableList<Uri> = mutableListOf()
)