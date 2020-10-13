package com.ojomono.ionce.firebase

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

object Storage {

    /***************/
    /** Constants **/
    /***************/

    // Path Strings
    private const val PS_IMAGES = "images"

    /************/
    /** Fields **/
    /************/

    // The Cloud Storage instance
    private val storage = Firebase.storage

    // Create a storage reference from our app
    var storageRef = storage.reference

    /********************/
    /** Public methods **/
    /********************/

    /**
     * Upload the given [file] to Storage, with name=[uid], and return the download Url [Task].
     */
    fun uploadUserPhoto(uid: String, file: Uri): Task<Uri> {
        val imageRef = storageRef.child("$PS_IMAGES/$uid")
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

}