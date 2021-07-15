package com.ojomono.ionce.firebase.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.ojomono.ionce.firebase.Authentication
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.firebase.Utils
import com.ojomono.ionce.models.GroupModel
import com.ojomono.ionce.models.UserItemModel
import com.ojomono.ionce.models.UserModel
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
    private val _userGroup = MutableLiveData<GroupModel>()
    val userGroup: LiveData<GroupModel> = _userGroup

    init {
        Database.userData.observeForever { switchGroupData(it?.group) }
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

        val displayName =
            Authentication.currentUser.value?.displayName?.let {
                if (it.isNotBlank()) it else userRef.id
            } ?: userRef.id

        // TODO avoid referring to Authentication object here - make a UserRepository object
        // Set the current user as the only user in the group
        val group = GroupModel(groupRef.id, hashMapOf(userRef.id to UserItemModel(displayName)))

        // In a batch write: update user's tale list, and run given action on the tale ref
        Database.runBatch {
            it.update(userRef, UserModel::group.name, groupRef.id)
            it.set(groupRef, group)
        }.continueWithTask { task ->
            // Return a task holding the success state and the group's document reference
            if (task.isSuccessful) Tasks.forResult(groupRef) else Tasks.forCanceled()
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
                    _userGroup.value = snapshot?.toObject(GroupModel::class.java)
                }
            }
        }

        return task
    }
}