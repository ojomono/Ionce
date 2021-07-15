package com.ojomono.ionce.models

import com.google.firebase.firestore.DocumentId

/**
 * Data model to hold all the group's model (saved in the 'groups' collection)
 */
data class GroupModel(
    @DocumentId val id: String = "",
    val members: HashMap<String, UserItemModel> = hashMapOf()
)
