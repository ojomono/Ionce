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

        //-------------------------
        // Version 1.0.1 constants:
        //-------------------------

        // Path string used in Storage object to upload the file like so:
        // Firebase.storage.reference.child("$PS_IMAGES/${user.uid}").putFile(file)
        val PS_IMAGES = "images"

        // Which leads to a download url that looked like so:
        // https://firebasestorage.googleapis.com/v0/b/<bucket>/o/images%2F<UID>?alt=media&token=<token>
        val storageRef = Firebase.storage.reference
        val V1_0_1_DOWNLOAD_URL =
            "https://firebasestorage.googleapis.com/v0/b/${storageRef.bucket}/o/$PS_IMAGES%2F${user.uid}"

        //---------------------------------------
        // If user photo url is in v1.0.1 format:
        //---------------------------------------
        if (user.photoUrl.toString().startsWith(V1_0_1_DOWNLOAD_URL)) {

            // Download the given image from the storage
            val oldRef = storageRef.child("$PS_IMAGES/${user.uid}")
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
                            (!FirebaseAuth.getInstance().currentUser?.photoUrl.toString()
                                .startsWith(V1_0_1_DOWNLOAD_URL))

                        // Delete the previously uploaded data from firebase. (v1.0.1 style)
                        ) oldRef.delete()
                    }
            }
        }
    }
}