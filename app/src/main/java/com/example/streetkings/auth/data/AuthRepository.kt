package com.example.streetkings.auth.data

import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.flow.Flow
import kotlin.Result
import android.net.Uri
import com.example.streetkings.core.data.User


interface AuthRepository {

    fun createUser(email: String, pass: String): Flow<Result<AuthResult>>

    fun loginUser(email: String, pass: String): Flow<Result<AuthResult>>

    fun registerUserWithDetails(email: String, pass: String, user: User, imageUri: Uri?): Flow<Result<Unit>>
}