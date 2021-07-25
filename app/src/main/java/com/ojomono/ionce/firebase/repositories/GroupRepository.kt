package com.ojomono.ionce.firebase.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.WriteBatch
import com.ojomono.ionce.firebase.Authentication
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.firebase.Utils
import com.ojomono.ionce.models.*
import com.ojomono.ionce.utils.TAG

object GroupRepository {

    /***************/
    /** Constants **/
    /***************/

    // Collection Paths
    private const val CP_GROUPS = "groups"

    /************/
    /** Fields **/
    /************/

    // Always keep current group's document loaded
    private var groupDocRef: DocumentReference? = null
    private var groupDocument: DocumentSnapshot? = null
    private var groupRegistration: ListenerRegistration? = null

    // Current user's group
    private val _group = MutableLiveData<GroupModel>()
    val group: LiveData<GroupModel> = _group

    // Observe current user's group
    init {
        Database.userData.observeForever { switchGroupData(it.group) }
    }

    /********************/
    /** public methods **/
    /********************/

    /**
     * Create a new group in the db, and add current user to it.
     */
    fun createGroup() = Database.userDocRef?.let { userRef ->

        // Get reference for a new group doc
        val groupRef = Database.collection(CP_GROUPS).document()

        // Set the current user as the only user in the group
        val group = GroupModel(groupRef.id, hashMapOf(userRef.id to buildUserItem(userRef.id)))

        // In a batch write: update user's group and set the new group
        runGroupBatch(groupRef) { set(groupRef, group) }

    } ?: throw Utils.NoSignedInUserException

    /**
     * Set [groupId] as user's group and add the user to the groups members.
     */
    fun joinGroup(groupId: String) = Database.userDocRef?.let { userRef ->

        // Get reference for the given group
        val groupRef = Database.collection(CP_GROUPS).document(groupId)

        // In a batch write: update user's group, and add user to the group's members
        runGroupBatch(groupRef) {
            update(
                groupRef,
                "${GroupModel::members.name}.${userRef.id}",
                buildUserItem(userRef.id)
            )
        }

    } ?: throw Utils.NoSignedInUserException

    /*********************/
    /** private methods **/
    /*********************/

    /**
     * Switch the current group document reference and snapshot to those of the group with the given
     * [id] - and listen to changes. Return the get [Task].
     */
    private fun switchGroupData(id: String?): Task<DocumentSnapshot>? {

        var task: Task<DocumentSnapshot>? = null

        // If no id was given - the user has no group
        if (id.isNullOrEmpty()) {
            groupRegistration?.remove()
            groupDocRef = null
            groupDocument = null

            // If the new id belongs to another group than the one we currently refer to
        } else if (!groupDocRef?.id.equals(id)) {

            // If a change listener is registered to the previous group's document - remove it
            groupRegistration?.remove()

            // Get the current group's document reference
            groupDocRef = Database.collection(CP_GROUPS).document(id)

            // Save the document locally
            task = groupDocRef?.get()
            task?.addOnSuccessListener { groupDocument = it }
                ?.addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }

            // Listen for changes in the document
            groupDocRef?.addSnapshotListener { snapshot, e ->
                if (e != null) Log.w(TAG, "Listen failed.", e)
                else {
                    groupDocument = snapshot
                    _group.value = snapshot?.toObject(GroupModel::class.java)
                }
            }
        }

        return task
    }

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
     * In a batch write, run the given [action] on the given [groupRef] and update the current
     * user's group to [groupRef].id.
     */
    private fun runGroupBatch(
        groupRef: DocumentReference,
        action: WriteBatch.(DocumentReference) -> WriteBatch
    ) = Database.userDocRef?.let { userRef ->

        // In a batch write: update user's group, and run given action on the group ref
        Database.runBatch {
            it.update(userRef, UserModel::group.name, groupRef.id)
            it.action(groupRef)
        }.continueWithTask { task ->
            // Return a task holding the success state and the group's document reference
            if (task.isSuccessful) Tasks.forResult(groupRef) else Tasks.forCanceled()
        }
    } ?: throw Utils.NoSignedInUserException

}