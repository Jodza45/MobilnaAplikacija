package com.example.streetkings.core.data

import com.google.firebase.firestore.GeoPoint

data class User(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val profilePictureUrl: String = "",
    val points: Int = 0
)