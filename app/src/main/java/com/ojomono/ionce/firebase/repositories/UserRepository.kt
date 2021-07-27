package com.ojomono.ionce.firebase.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.ojomono.ionce.firebase.Authentication
import com.ojomono.ionce.firebase.Database
import com.ojomono.ionce.firebase.Utils.CP_USERS
import com.ojomono.ionce.models.UserModel

object UserRepository : DocHoldingRepo<UserModel>(UserModel::class.java, CP_USERS) {

    // Observe the logged-in user
    init {
        Authentication.currentUser.observeForever { switchDocument(it?.uid ?: "") }
    }

    /*********************/
    /** private methods **/
    /*********************/

    override fun switchDocument(id: String): Task<DocumentSnapshot>? {
        val task = super.switchDocument(id)

        // If the document does not exist yet - initialize it, as a continuation task
        task?.continueWithTask { getTask ->
            if (getTask.result?.exists() == true) null
            else Database.collection(CP_USERS).document(getTask.result.id).set(UserModel())
        }

        return task
    }
}