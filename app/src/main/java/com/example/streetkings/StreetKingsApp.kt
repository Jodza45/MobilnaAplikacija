package com.example.streetkings

import android.app.Application
import com.cloudinary.android.MediaManager
import com.example.streetkings.core.data.Config
import kotlin.collections.mapOf

class StreetKingsApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = mapOf(
            "cloud_name" to Config.CLOUDINARY_CLOUD_NAME,
            "api_key" to Config.CLOUDINARY_API_KEY,
            "api_secret" to Config.CLOUDINARY_API_SECRET
        )
        MediaManager.init(this, config)
    }
}