package com.example.streetkings.auth.data

import android.net.Uri
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.streetkings.core.data.User
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import com.cloudinary.android.MediaManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepositoryImpl : AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun createUser(email: String, pass: String): Flow<Result<AuthResult>> = flow {
        try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun loginUser(email: String, pass: String): Flow<Result<AuthResult>> = flow {
        try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun registerUserWithDetails(
        email: String,
        pass: String,
        user: User,
        imageUri: Uri?
    ): Flow<Result<Unit>> = flow {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val userId = authResult.user?.uid ?: throw Exception("Greška: UID nije pronađen")

            var imageUrl = ""
            if (imageUri != null) {
                imageUrl = uploadImageToCloudinary(imageUri)
            }

            val finalUser = user.copy(
                uid = userId,
                email = email,
                profilePictureUrl = imageUrl
            )
            firestore.collection("users").document(userId).set(finalUser).await()

            emit(Result.success(Unit))

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    private suspend fun uploadImageToCloudinary(imageUri: Uri): String {
        return suspendCancellableCoroutine { continuation ->
            MediaManager.get().upload(imageUri).callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val url = resultData?.get("secure_url")?.toString()
                    if (url != null) {
                        continuation.resume(url)
                    } else {
                        continuation.resumeWithException(Exception("Cloudinary upload failed: URL is null"))
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    continuation.resumeWithException(Exception("Cloudinary upload failed: ${error?.description}"))
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
        }
    }
}