package com.ojomono.ionce.models

import com.google.firebase.firestore.DocumentId

/**
 * Data model to hold all the user's model (saved in the 'users' collection with [id] = user's uid
 * from firebase authentication)
 */
data class UserModel(
    @DocumentId override val id: String = "",
    val tales: MutableList<TaleItemModel> = mutableListOf(),
    val group: String = ""
) : BaseModel()