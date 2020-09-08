package com.ojomono.ionce.firebase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ojomono.ionce.models.TaleModel
import com.ojomono.ionce.models.UserModel
import com.ojomono.ionce.models.TaleItemModel
import com.ojomono.ionce.utils.TAG


/**
 * Handles all interactions with Firebase database.
 */
object Database {

    /***************/
    /** Constants **/
    /***************/

    // Collection Paths
    private const val CP_USERS = "users"
    private const val CP_TALES = "tales"

    /*************/
    /** Members **/
    /*************/

    // The Cloud Firestore instance
    private val db = Firebase.firestore

    // Current user's document
    private var userDocRef: DocumentReference? = null
    private var userDocument: DocumentSnapshot? = null
    private var registration: ListenerRegistration? = null

    // Current user's tales list
    private val _userTales = MutableLiveData<List<TaleItemModel>>()
    val userTales: LiveData<List<TaleItemModel>> = _userTales

    // If a user is already logged-in - get it's document
    init {
        Authentication.currentUser.value?.uid?.let { switchUserDocument(it) }
    }

    /**************/
    /** methods **/
    /*************/

    /**
     * Switch the current user document reference and snapshot to those of the user with the given
     * [id] - and listen to changes.
     */
    fun switchUserDocument(id: String?) {

        // If no id was given - no user is logged-in
        if (id.isNullOrEmpty()) {
            registration?.remove()
            userDocRef = null
            userDocument = null

            // If the new id belongs to another user than the one we currently refer to
        } else if (!userDocRef?.id.equals(id)) {

            // If a change listener is registered to the previous user's document - remove it
            registration?.remove()

            // Get the current user's document reference
            userDocRef = db.collection(CP_USERS).document(id)

            // Save the document locally
            userDocRef?.get()?.addOnSuccessListener { document ->
                userDocument = document
                // If the document does not exist yet - initialize it
                if (!document.exists()) userDocRef?.set(UserModel())
            }?.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

            // Listen for changes in the document
            registration = userDocRef?.addSnapshotListener { snapshot, e ->
                if (e != null) Log.w(TAG, "Listen failed.", e)
                else {
                    userDocument = snapshot
                    _userTales.value = snapshot?.toObject(UserModel::class.java)?.tales
                }
            }
        }
    }

    /**
     * Overwrite the tale document with id=[id] to have the given [title]. If [id] is empty, create
     * a new document with a generated id and title=[title].
     */
    fun setTale(id: String = "", title: String) {
        // Setting a tale is possible only if a user is logged in
        // (== his document reference is not null)
        userDocRef?.let { userRef ->
            // Create reference for wanted tale, for use inside the transaction - if an id was given
            // get the existing document, else reference a new one.
            val talesCol = userRef.collection(CP_TALES)
            val taleRef =
                if (id.isEmpty()) talesCol.document() else talesCol.document(id)
            val tale = TaleModel(taleRef.id, title)

            // In a transaction, set the tale's document and update the user's tale list
            db.runTransaction { transaction ->
                val user: UserModel? = transaction.get(userRef).toObject(UserModel::class.java)
                user?.let {
                    // Update user's list
                    user.setTale(TaleItemModel(tale))

                    // Commit to Firestore
                    transaction.update(userRef, user::tales.name, user.tales)
                    transaction.set(taleRef, tale)
                }
            }.addOnSuccessListener { Log.d(TAG, "Transaction success!") }
                .addOnFailureListener { e -> Log.w(TAG, "Transaction failure.", e) }
        }
    }

    /**
     * Overwrite the tale document with id=[taleItem].id to have the given [taleItem]'s title. If [taleItem].id
     * is empty, create a new document with a generated id and title=[taleItem].title.
     */
    fun setTale(taleItem: TaleItemModel) = setTale(taleItem.id, taleItem.title)

    /**
     * Delete the tale document with id=[id].
     */
    fun deleteTale(id: String) {
        // Deleting a tale is possible only if a user is logged in
        // (== his document reference is not null)
        userDocRef?.let { userRef ->
            // Create reference for wanted tale, for use inside the transaction
            val taleRef = userRef.collection(CP_TALES).document(id)

            // In a transaction, delete the tale document and remove it from user's tales list
            db.runTransaction { transaction ->
                val user: UserModel? = transaction.get(userRef).toObject(UserModel::class.java)
                user?.let {
                    // Update user's list
                    user.deleteTale(id)

                    // Commit to Firestore
                    transaction.update(userRef, user::tales.name, user.tales)
                    transaction.delete(taleRef)
                }
            }.addOnSuccessListener { Log.d(TAG, "Transaction success!") }
                .addOnFailureListener { e -> Log.w(TAG, "Transaction failure.", e) }
        }
    }

    /**
     * Delete the tale document with id=[taleItem].id.
     */
    fun deleteTale(taleItem: TaleItemModel) = deleteTale(taleItem.id)
}