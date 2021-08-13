package com.ojomono.ionce.models

/**
 * Data model to hold only the user's model relevant to the user's list screen (saved as a list in
 * the group's document, each with the [id] generated for the matching 'user' document)
 */
data class UserItemModel(
    override val id: String = "",
    val displayName: String = "",
    val tales: MutableList<TaleItemModel> = mutableListOf()
) : BaseItemModel() {

    companion object {
        private const val FALLBACK_NAME_LENGTH = 8
    }

    /**
     * Create a [UserItemModel] from a given full [user]. Use separate [displayName] param when it's
     * given, or a part of the uid if not.
     */
    constructor(user: UserModel, displayName: String? = null) :
            this(
                user.id,
                displayName ?: user.id.take(FALLBACK_NAME_LENGTH),
                user.tales
            )
}