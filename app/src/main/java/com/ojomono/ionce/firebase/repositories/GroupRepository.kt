package com.ojomono.ionce.firebase.repositories

import com.google.firebase.firestore.*
import com.ojomono.ionce.firebase.Authentication
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.firebase.Utils
import com.ojomono.ionce.firebase.Utils.CP_GROUPS
import com.ojomono.ionce.models.*

object GroupRepository : DocHoldingRepo<GroupModel>(GroupModel::class.java, CP_GROUPS) {

    // Observe current user's group
    init {
        UserRepository.model.observeForever { switchDocument(it?.group ?: "") }
    }

    /********************/
    /** public methods **/
    /********************/

    /**
     * Create a new group in the db, and add current user to it.
     */
    fun createGroup() = UserRepository.docRef?.let { userRef ->

        // Get reference for a new group doc
        val groupRef = Database.collection(CP_GROUPS).document()

        // Set the current user as the only user in the group
        val group = GroupModel(groupRef.id, hashMapOf(userRef.id to buildUserItem(userRef.id)))

        // In a batch write: update user's group and set the new group
        runGroupBatch(groupRef.id) { set(groupRef, group) }

    } ?: throw Utils.NoSignedInUserException

    /**
     * Set [groupId] as user's group and add the user to the groups members.
     */
    fun joinGroup(groupId: String) = UserRepository.docRef?.let { userRef ->

        // Get reference for the given group
        val groupRef = Database.collection(CP_GROUPS).document(groupId)

        // In a batch write: update user's group, and add user to the group's members
        runGroupBatch(groupRef.id) {
            update(
                groupRef,
                "${GroupModel::members.name}.${userRef.id}",
                buildUserItem(userRef.id)
            )
        }

    } ?: throw Utils.NoSignedInUserException

    /**
     * Remove current user from his group, and set user's group to empty.
     * If user is last in the group - delete the group.
     */
    fun leaveGroup() = UserRepository.docRef?.let { userRef ->
        docRef?.let { groupRef ->

            // In a batch write: clear user's group, and remove the user from the group members
            runGroupBatch("") {

                // If user is the only member - delete the group
                if (model.value?.members?.any { it.key != userRef.id } == false) delete(groupRef)
                else update(
                    groupRef,
                    "${GroupModel::members.name}.${userRef.id}",
                    FieldValue.delete()
                )
            }

        } ?: throw Utils.UserNotInGroupException
    } ?: throw Utils.NoSignedInUserException

    /*********************/
    /** private methods **/
    /*********************/

    /**
     * Get a [UserItemModel] for the given [uid].
     */
    private fun buildUserItem(uid: String): UserItemModel {

        // TODO avoid referring to Authentication object here - make a UserRepository object
        val displayName =
            Authentication.currentUser.value?.displayName?.let {
                if (it.isNotBlank()) it else uid
            } ?: uid

        return UserItemModel(displayName)
    }

    /**
     * In a batch write, run the given [action] and update the current user's group to [id].
     */
    private fun runGroupBatch(id: String, action: WriteBatch.() -> WriteBatch) =
        UserRepository.docRef?.let { userRef ->

            // In a batch write: update user's group, and run given action on the group ref
            Database.runBatch {
                it.update(userRef, UserModel::group.name, id)
                it.action()
            }
        } ?: throw Utils.NoSignedInUserException

}