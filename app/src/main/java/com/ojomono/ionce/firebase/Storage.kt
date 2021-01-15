package com.ojomono.ionce.firebase

// TODO: Avoid Android imports and move to separated module when needed for more UI platforms
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ojomono.ionce.utils.Utils
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
    var storageRef = storage.reference

    // Keep the current user's storage path updated based on the current user
    private var userPath: String? = null

    init {
        Authentication.currentUser.observeForever { userPath = it?.let { "/$PS_USERS/${it.uid}" } }
    }

    /********************/
    /** Public methods **/
    /********************/

    /**
     * Upload the given [file] to Storage, as the photo of the current logged user, and return the
     * download Url [Task].
     */
    fun uploadUserPhoto(file: Uri): Task<Uri>? =
        userPath?.let { uploadFile("$it/$PS_USER_PHOTO", file) }

    /**
     * Upload [file] to Storage, as media of the tale with given [taleId] and return the download
     * Url [Task].
     */
    fun uploadTaleCover(taleId: String, file: Uri): Task<Uri>? =
        userPath?.let { uploadFile("$it/$taleId/${generateUniqueName(file)}", file) }

    /**
     * Delete the given [fileName] from Storage.
     */
    fun deleteFile(fileName: Uri) = storage.getReferenceFromUrl(fileName.toString()).delete()

    /********************/
    /** Private methods **/
    /********************/

    /**
     * Upload the given [file] to the given [path] in Storage, and return the download Url [Task].
     */
    private fun uploadFile(path: String, file: Uri): Task<Uri> {
        val imageRef = storageRef.child(path)
        val uploadTask = imageRef.putFile(file)

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
     * the extension of [file] (if has one).
     */
    private fun generateUniqueName(file: Uri): String {
        val str = file.lastPathSegment ?: ""
        val extension =
            if (str.contains(".")) str.substring(str.lastIndexOf(".")) else ""
        val uuid = UUID.randomUUID().toString()

        return "$uuid/$PS_IMAGE$extension"
    }

}