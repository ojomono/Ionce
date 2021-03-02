package com.ojomono.ionce.firebase

// TODO: Avoid Android imports and move to separated module when needed for more UI platforms
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.ojomono.ionce.utils.ImageUtils.COMPRESS_FORMAT
import com.ojomono.ionce.utils.TAG
import java.util.*

object Storage {

    /***************/
    /** Constants **/
    /***************/

    // Path Strings
    private const val PS_USERS = "users"
    private const val PS_USER_PHOTO = "userPhoto"
    private const val PS_IMAGE = "image"

    /************/
    /** Fields **/
    /************/

    // The Cloud Storage instance
    private val storage = Firebase.storage

    // Create a storage reference from our app
    private val storageRef = storage.reference

    // Keep the current user's storage path updated based on the current user
    private var userRef: StorageReference? = null

    init {
        Authentication.currentUser.observeForever {
            userRef = it?.let { storageRef.child("/$PS_USERS/${it.uid}") }
        }
    }

    /********************/
    /** Public methods **/
    /********************/

    /**
     * Upload the given [image] to Storage, as the photo of the current logged user, and return
     * the downloadUrl [Task].
     */
    fun uploadUserPhoto(image: Uri) = userRef?.let { userRef ->
        uploadFile(userRef.child("$PS_USER_PHOTO.${COMPRESS_FORMAT.name}"), image)
    } ?: throw Utils.NoSignedInUserException

    /**
     * Upload the given [image] to Storage, as media of the given [taleId], and return the
     * downloadUrl [Task].
     */
    fun uploadTaleMedia(taleId: String, image: Uri) = userRef?.let { userRef ->
        uploadFile(
            userRef.child("$taleId/${generateUniqueName()}.${COMPRESS_FORMAT.name}"),
            image
        )
    } ?: throw Utils.NoSignedInUserException

    /**
     * Get all active upload task for given [taleId].
     */
    fun getActiveTaleTasks(taleId: String) = userRef?.let { userRef ->
        val pathPrefix = "${userRef.path}/$taleId"
        storageRef.activeUploadTasks.filter { it.snapshot.storage.path.startsWith(pathPrefix) }
    } ?: throw Utils.NoSignedInUserException

    /**
     * Upload the given [file] to the given [destUrl].
     */
    fun uploadFileToDest(destUrl: String, file: Uri) =
        uploadFile(storage.getReferenceFromUrl(destUrl), file)

    /**
     * Delete the given [fileUrl] from Storage, and return delete [Task].
     */
    fun deleteFile(fileUrl: String) = storage.getReferenceFromUrl(fileUrl).delete()

    /**
     * Delete all files in [filesList] from Storage in one continued [Task] and return it. If no
     * delete was needed ([filesList] is empty), return null.
     */
    fun deleteFiles(filesList: List<String>): Task<Void>? {
        var task: Task<Void>? = null
        for (file in filesList)
            task = Utils.continueWithTaskOrInNew(task, true) { deleteFile(file) }
        return task
    }

    /*********************/
    /** Private methods **/
    /*********************/

    /**
     * Upload the given [image] to the given [destRef] in Storage, and return the downloadUrl [Task]
     */
    private fun uploadFile(destRef: StorageReference, image: Uri): Task<Uri> {
        val uploadTask = destRef.putFile(image)

        // Return the task getting the download URL
        return uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) task.exception?.let { e -> throw e }
            destRef.downloadUrl
        }
    }

    /**
     * Generate a unique name like so: "<uuid>/image"
     */
    private fun generateUniqueName() = "${UUID.randomUUID()}/$PS_IMAGE"
}