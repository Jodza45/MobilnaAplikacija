package com.example.streetkings.leaderboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetkings.core.data.User
import com.example.streetkings.leaderboard.data.LeaderboardRepository
import com.example.streetkings.leaderboard.data.LeaderboardRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel : ViewModel() {
    private val repository: LeaderboardRepository = LeaderboardRepositoryImpl()

    private val _leaderboardState = MutableStateFlow<LeaderboardState>(LeaderboardState.Loading)
    val leaderboardState = _leaderboardState.asStateFlow()

    init {
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            repository.getUsersSortedByPoints().collect { result ->
                result.onSuccess { users ->
                    _leaderboardState.value = LeaderboardState.Success(users)
                }.onFailure {
                    _leaderboardState.value = LeaderboardState.Error(it.message ?: "Greška")
                }
            }
        }
    }
}

sealed class LeaderboardState {
    object Loading : LeaderboardState()
    data class Success(val users: List<User>) : LeaderboardState()
    data class Error(val message: String) : LeaderboardState()
}