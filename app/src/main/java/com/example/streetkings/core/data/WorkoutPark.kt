package com.example.streetkings.core.data

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class WorkoutPark(
    val id: String = "",
    val name: String = "",
    val location: GeoPoint? = null, // Specijalan Firebase tip za koordinate
    val addedByUid: String = "",
    // Kasnije ćemo dodati polja za opremu, ocene, itd.

    @ServerTimestamp // Firebase će automatski upisati vreme na serveru
    val createdAt: Date? = null,
    val equipment: List<String> = emptyList(), // Lista opreme, npr. ["Vratilo", "Razboj"]
    val rating: Float = 0.0f, // Ocena od 0 do 5
    val photoUrls: List<String> = emptyList(),
    val ratedBy: List<String> = emptyList()
)