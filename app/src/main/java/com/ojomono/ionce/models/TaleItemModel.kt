package com.ojomono.ionce.models

import com.google.firebase.firestore.Exclude

/**
 * Data model to hold only the tale's model relevant to the tale's list screen (saved as a list in
 * the user's document, each with the [id] generated for the matching 'tale' document)
 */
data class TaleItemModel(
    override val id: String = "",
    val title: String = "",
    val cover: String = "",
    @get:Exclude var ownerId: String = "",
    @get:Exclude var ownerName: String = "",
) : BaseItemModel() {
    /**
     * Create a [TaleItemModel] from a given full [tale]. Use separate [id] param when it's a new
     * tale and the new generated id is not part of the full [tale].
     */
    constructor(tale: TaleModel, id: String? = null) :
            this(id ?: tale.id, tale.title, tale.media.firstOrNull() ?: "")
}