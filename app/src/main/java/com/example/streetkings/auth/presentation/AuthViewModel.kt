package com.example.streetkings.auth.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetkings.auth.data.AuthRepository
import com.example.streetkings.auth.data.AuthRepositoryImpl
import com.example.streetkings.core.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authRepository: AuthRepository = AuthRepositoryImpl()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    fun registerUser(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.createUser(email, pass).collect { result ->
                result.onSuccess {
                    _authState.value = AuthState.Success("Uspešna registracija!")
                }.onFailure {
                    _authState.value = AuthState.Error(it.message ?: "Došlo je do greške")
                }
            }
        }
    }

    fun loginUser(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.loginUser(email, pass).collect { result ->
                result.onSuccess {
                    //Log.d("LOGIN_PROCESS", "Uspešna prijava! Postavljam stanje na Success.")
                    _authState.value = AuthState.Success("Uspešna prijava!")
                }.onFailure {
                    _authState.value = AuthState.Error(it.message ?: "Pogrešni podaci")
                }
            }
        }
    }

    fun registerUserWithDetails(email: String, pass: String, firstName: String, lastName: String, phone: String, imageUri: Uri?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading


            val user = User(
                firstName = firstName,
                lastName = lastName,
                phone = phone
            )


            authRepository.registerUserWithDetails(email, pass, user, imageUri)
                .collect { result ->
                    result.onSuccess {
                        _authState.value = AuthState.Success("Korisnik uspešno registrovan!")
                    }.onFailure {

                        _authState.value = AuthState.Error(it.message ?: "Greška pri registraciji")
                    }
                }
        }
    }
}

sealed class AuthState {
    data object Initial : AuthState()
    data object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}