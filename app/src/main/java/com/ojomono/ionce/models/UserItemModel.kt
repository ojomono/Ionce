package com.ojomono.ionce.models

/**
 * Data model to hold only the user's model relevant to the user's list screen (saved as a list in
 * the group's document, each with the [id] generated for the matching 'user' document)
 */
data class UserItemModel(val displayName: String = "") {
}