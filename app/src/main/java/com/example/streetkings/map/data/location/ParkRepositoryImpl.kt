package com.example.streetkings.map.data

import android.util.Log
import com.example.streetkings.core.data.UserLocation
import com.example.streetkings.core.data.WorkoutPark
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query

class ParkRepositoryImpl : ParkRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun addPark(park: WorkoutPark): Flow<Result<Unit>> = flow {
        try {
            val documentRef = firestore.collection("parks").document()

            val finalPark = park.copy(id = documentRef.id)
            documentRef.set(finalPark).await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getParks(equipmentFilter: List<String>, minRatingFilter: Float): Flow<Result<List<WorkoutPark>>> = flow {
        try {
            var query: Query = firestore.collection("parks")

            if (minRatingFilter > 0) {
                query = query.whereGreaterThanOrEqualTo("rating", minRatingFilter)
            }

            if (equipmentFilter.isNotEmpty()) {
                query = query.whereArrayContains("equipment", equipmentFilter.first())
            }

            val snapshot = query.get().await()
            val parks = snapshot.toObjects(WorkoutPark::class.java)

            val finalParks = if (equipmentFilter.size > 1) {
                parks.filter { park -> park.equipment.containsAll(equipmentFilter) }
            } else {
                parks
            }

            Log.d("GET_PARKS", "Repozitorijum dohvatio ${finalParks.size} filtriranih parkova.")
            emit(Result.success(finalParks))

        } catch (e: Exception) {
            Log.e("GET_PARKS", "Greška u repozitorijumu pri filtriranju: ${e.message}")
            emit(Result.failure(e))
        }
    }

    override fun getUserLocations(currentUserId: String): Flow<Result<Map<String, UserLocation>>> = flow {
        try {
            val snapshot = firestore.collection("user_locations").get().await()

            val userLocations = snapshot.documents
                .filter { it.id != currentUserId }
                .associate { document ->
                    document.id to document.toObject(UserLocation::class.java)!!
                }
            emit(Result.success(userLocations))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun awardPoints(userId: String, points: Int): Flow<Result<Unit>> = flow {
        try {
            val userDocRef = firestore.collection("users").document(userId)

            userDocRef.update("points", FieldValue.increment(points.toLong())).await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getParkById(parkId: String): Flow<Result<WorkoutPark?>> = flow {
        try {
            val snapshot = firestore.collection("parks").document(parkId).get().await()
            val park = snapshot.toObject(WorkoutPark::class.java)
            emit(Result.success(park))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun ratePark(parkId: String, currentUserId: String, newRating: Float): Flow<Result<Unit>> = flow {
        try {
            val parkRef = firestore.collection("parks").document(parkId)
            val userRef = firestore.collection("users").document(currentUserId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(parkRef)
                val park = snapshot.toObject(WorkoutPark::class.java)
                    ?: throw Exception("Park nije pronađen")

                if (park.addedByUid == currentUserId) {
                    throw Exception("Ne možete oceniti sopstveni park.")
                }

                if (park.ratedBy.contains(currentUserId)) {
                    throw Exception("Već ste ocenili ovaj park.")
                }

                val totalRatings = park.ratedBy.size
                val currentTotalPoints = park.rating * totalRatings
                val newAverageRating = (currentTotalPoints + newRating) / (totalRatings + 1)

                transaction.update(parkRef, "rating", newAverageRating)
                transaction.update(parkRef, "ratedBy", FieldValue.arrayUnion(currentUserId))

                transaction.update(userRef, "points", FieldValue.increment(2))

                null
            }.await()

            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}