package com.example.streetkings.map.data

import com.example.streetkings.core.data.UserLocation
import com.example.streetkings.core.data.WorkoutPark
import kotlinx.coroutines.flow.Flow

interface ParkRepository {
    fun addPark(park: WorkoutPark): Flow<Result<Unit>>
    fun getParks(equipmentFilter: List<String>, minRatingFilter: Float): Flow<Result<List<WorkoutPark>>>
    fun getUserLocations(currentUserId: String): Flow<Result<Map<String, UserLocation>>>
    fun awardPoints(userId: String, points: Int): Flow<Result<Unit>>
    fun getParkById(parkId: String): Flow<Result<WorkoutPark?>>
    fun ratePark(parkId: String, currentUserId: String, newRating: Float): Flow<Result<Unit>>
}