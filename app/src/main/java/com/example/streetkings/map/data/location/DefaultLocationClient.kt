package com.example.streetkings.map.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.example.streetkings.map.domain.LocationClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
): LocationClient {

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {
            if(!context.hasLocationPermission()) {

                throw Exception("Aplikacija nema dozvolu za pristup lokaciji.")
            }

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager    //sistemski servisi
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)             //za dobijanje lokacije (provider)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)     //mrezni provider
            if(!isGpsEnabled && !isNetworkEnabled) {

                throw Exception("GPS nije uključen.")
            }

            val request = LocationRequest.create()              //zahtev za lokacijom pod nekim intervalima
                .setInterval(interval)
                .setFastestInterval(interval)

            val locationCallback = object : LocationCallback() {                //listener i uzima najnoviju lokaciju
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.locations.lastOrNull()?.let { location ->
                        launch { send(location) }
                    }
                }
            }

            client.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )

            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }
        }
    }
}

fun Context.hasLocationPermission(): Boolean {
    return checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
}