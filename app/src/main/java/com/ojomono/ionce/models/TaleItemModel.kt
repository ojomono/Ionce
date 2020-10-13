package com.ojomono.ionce.models

/**
 * Data model to hold only the tale's model relevant to the tale's list screen (saved as a list in
 * the user's document, each with the [id] generated for the matching 'tale' document)
 */
data class TaleItemModel(val id: String = "", var title: String = "") {
    /**
     * Create a [TaleItemModel] from a given full [tale].
     */
    constructor(tale: TaleModel) : this(tale.id, tale.title)
}