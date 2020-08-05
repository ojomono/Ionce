package com.ojomono.ionce.models

import com.google.firebase.firestore.DocumentId

data class Tale(@DocumentId val id: String = "", val title: String = "")