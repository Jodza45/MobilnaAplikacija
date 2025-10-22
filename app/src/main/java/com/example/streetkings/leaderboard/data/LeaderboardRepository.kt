package com.example.streetkings.leaderboard.data

import com.example.streetkings.core.data.User
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    fun getUsersSortedByPoints(): Flow<Result<List<User>>>
}