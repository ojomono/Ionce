package com.ojomono.ionce.firebase

import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File

/**
 * Used when the data structure in any of the Firebase services has been changed, in order to
 * convert all data to new structure.
 */
object Conversions {

    /**
     * Used to convert the user photo path in storage from the way it was in version 1.0.1, to the
     * refactored version.
     */
    fun fixUserPhotoPathInStorageIfNeeded(user: FirebaseUser) {

        val storage = Firebase.storage

        //-------------------------
        // Version 1.0.1 constants:
        //-------------------------

        // Path string used in Storage object to upload the file like so:
        // Firebase.storage.reference.child("$PS_IMAGES/${user.uid}").putFile(file)
        val PS_IMAGES = "images"
        val V1_0_1_PATH = "/$PS_IMAGES/${user.uid}"

        //-------------------------
        // Version 1.0.2 constants:
        //-------------------------

        // Path string used in Storage object to upload the file like so:
        // Firebase.storage.reference.child("$PS_USERS/${user.uid}/$PS_USER_PHOTO").putFile(file)
        val PS_USERS = "users"
        val PS_USER_PHOTO = "userPhoto.jpg"
        val V1_0_2_PATH = "/$PS_USERS/${user.uid}/$PS_USER_PHOTO"

        // Get the actual user photo url path
        try {
            val oldPath = storage.getReferenceFromUrl(user.photoUrl.toString()).path
            // An IllegalArgumentException is thrown if the url is not from Firebase Storage.

            //---------------------------------------
            // If user photo url is in v1.0.1 format:
            //---------------------------------------
            if (oldPath in listOf(V1_0_1_PATH, V1_0_2_PATH)) {

                // Download the given image from the storage
                val oldRef = storage.reference.child(oldPath)
                val localFile = File.createTempFile("images", "jpg")
                val localFileUri = localFile.toUri()
                oldRef.getFile(localFileUri).addOnSuccessListener {

                    // Update the photo url so it will be uploaded in the current used way
                    Authentication.updatePhotoUrl(localFileUri, true)
                        ?.addOnCompleteListener {

                            // Delete local temp file
                            localFile.delete()

                            // If update succeed, and the new url really is in different format
                            // Delete the previously uploaded data from firebase. (v1.0.1 style)
                            if (it.isSuccessful and
                                (storage.getReferenceFromUrl(
                                    FirebaseAuth.getInstance().currentUser?.photoUrl.toString()
                                ).path != oldPath)

                            // Delete the previously uploaded data from firebase. (v1.0.1 style)
                            ) oldRef.delete()
                        }
                }
            }

        } catch (e: IllegalArgumentException) {
            // user photo is not from storage - do nothing
        }
    }
}