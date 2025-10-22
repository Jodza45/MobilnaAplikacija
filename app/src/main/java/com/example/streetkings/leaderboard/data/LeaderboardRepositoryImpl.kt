package com.example.streetkings.leaderboard.data

import com.example.streetkings.core.data.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class LeaderboardRepositoryImpl : LeaderboardRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun getUsersSortedByPoints(): Flow<Result<List<User>>> = flow {
        try {
            val snapshot = firestore.collection("users")
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()

            val users = snapshot.toObjects(User::class.java)
            emit(Result.success(users))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}