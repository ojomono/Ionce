package com.ojomono.ionce.models

/**
 * Data model to hold only the tale's data relevant to the tale's list screen (saved as a list in
 * the user's document, each with the [id] generated for the matching 'tale' document)
 */
data class TalesItem(val id: String = "", var title: String = "") {
    /**
     * Create a [TalesItem] from a given full [tale].
     */
    constructor(tale: Tale) : this(tale.id, tale.title)
}