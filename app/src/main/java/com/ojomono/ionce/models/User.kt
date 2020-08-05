package com.ojomono.ionce.models

import com.google.firebase.firestore.DocumentId

data class User(@DocumentId val id: String = "", val tales: HashMap<String, String> = hashMapOf()) {
    fun addTale(tale: Tale) {
        tales[tale.id] = tale.title
    }
}