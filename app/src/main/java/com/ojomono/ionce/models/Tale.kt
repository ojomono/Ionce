package com.ojomono.ionce.models

import com.google.firebase.firestore.DocumentId

/**
 * Data model to hold all the tale's data (saved in the 'tales' sub-collection with generated [id]).
 */
data class Tale(@DocumentId val id: String = "", val title: String = "")