package com.example.streetkings.core.data

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class WorkoutPark(
    val id: String = "",
    val name: String = "",
    val location: GeoPoint? = null,
    val addedByUid: String = "",


    @ServerTimestamp
    val createdAt: Date? = null,
    val equipment: List<String> = emptyList(),
    val rating: Float = 0.0f,
    val photoUrls: List<String> = emptyList(),
    val ratedBy: List<String> = emptyList()
)