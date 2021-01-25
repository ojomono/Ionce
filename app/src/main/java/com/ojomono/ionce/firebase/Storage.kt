package com.ojomono.ionce.firebase

// TODO: Avoid Android imports and move to separated module when needed for more UI platforms
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ojomono.ionce.utils.ImageUtils.COMPRESS_FORMAT
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
    private var storageRef = storage.reference

    // Keep the current user's storage path updated based on the current user
    private var userPath: String? = null

    init {
        Authentication.currentUser.observeForever { userPath = it?.let { "/$PS_USERS/${it.uid}" } }
    }

    /********************/
    /** Public methods **/
    /********************/

    /**
     * Upload the given [uri] to Storage, as the photo of the current logged user, and return
     * the download Url [Task].
     */
    fun uploadUserPhoto(uri: Uri): Task<Uri> =
        userPath?.let { uploadFile("$it/$PS_USER_PHOTO.${COMPRESS_FORMAT.name}", uri) }
            ?: throw Utils.NoSignedInUserException

    /**
     * Upload the given [uri] to Storage, as media of the tale with given [taleId] and return the
     * download Url [Task].
     */
    fun uploadTaleCover(taleId: String, uri: Uri): Task<Uri> =
        userPath?.let {
            uploadFile("$it/$taleId/${generateUniqueName()}.${COMPRESS_FORMAT.name}", uri)
        } ?: throw Utils.NoSignedInUserException

    /**
     * Delete the given [fileName] from Storage, and return delete [Task].
     */
    fun deleteFile(fileName: String) = storage.getReferenceFromUrl(fileName).delete()

    /**
     * Delete all files in [filesList] from Storage in one continued [Task] and return it. If no
     * delete was needed ([filesList] is empty), return null.
     */
    fun deleteFiles(filesList: List<String>): Task<Void>? {
        var task: Task<Void>? = null
        for (file in filesList) task = Utils.continueWithTaskOrInNew(task) {

            // If any delete fails, stop deleting and return the failed task
            if (it?.isSuccessful != false) deleteFile(file)
            else it
        }
        return task
    }

    /********************/
    /** Private methods **/
    /********************/

    /**
     * Upload the given [uri] to the given [path] in Storage, and return the download Url [Task].
     */
    private fun uploadFile(path: String, uri: Uri): Task<Uri> {
        val imageRef = storageRef.child(path)
        val uploadTask = imageRef.putFile(uri)

        // Return the task getting the download URL
        return uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }
    }

    /**
     * Generate a unique name like so: "<uuid>/image.<ext>" where uuid is a random UUID and ext is
     * the [extension].
     */
    private fun generateUniqueName(extension: String = "") =
        "${UUID.randomUUID()}/$PS_IMAGE$extension"

}