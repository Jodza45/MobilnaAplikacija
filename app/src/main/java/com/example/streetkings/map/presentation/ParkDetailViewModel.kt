package com.example.streetkings.map.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetkings.core.data.WorkoutPark
import com.example.streetkings.map.data.ParkRepository
import com.example.streetkings.map.data.ParkRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ParkDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val repository: ParkRepository = ParkRepositoryImpl()

    private val _parkState = MutableStateFlow<ParkDetailState>(ParkDetailState.Loading)
    val parkState = _parkState.asStateFlow()

    private val parkId: String = checkNotNull(savedStateHandle["parkId"])

    init {
        loadParkDetails()
    }

    private fun loadParkDetails() {
        viewModelScope.launch {
            repository.getParkById(parkId).collect { result ->
                result.onSuccess { park ->
                    if (park != null) {
                        _parkState.value = ParkDetailState.Success(park)
                    } else {
                        _parkState.value = ParkDetailState.Error("Park nije pronađen.")
                    }
                }.onFailure {
                    _parkState.value = ParkDetailState.Error(it.message ?: "Greška")
                }
            }
        }
    }

    fun submitRating(rating: Int) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid    //logovan user
            if (userId == null) {
                _parkState.value = ParkDetailState.Error("Morate biti ulogovani.")
                return@launch
            }

            repository.ratePark(parkId, userId, rating.toFloat()).collect { result ->
                result.onSuccess {
                    loadParkDetails()
                }.onFailure {
                    _parkState.value = ParkDetailState.Error(it.message ?: "Greška pri ocenjivanju.")
                }
            }
        }
    }
}

sealed class ParkDetailState {
    object Loading : ParkDetailState()
    data class Success(val park: WorkoutPark) : ParkDetailState()
    data class Error(val message: String) : ParkDetailState()
}