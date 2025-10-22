package com.example.streetkings.map.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetkings.map.data.ParkRepository
import com.example.streetkings.map.data.ParkRepositoryImpl
import com.example.streetkings.map.data.location.DefaultLocationClient
import com.example.streetkings.map.domain.calculateDistance
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.MapProperties
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import android.content.Context
import android.app.NotificationManager
import com.example.streetkings.R
import android.os.Build
import android.app.NotificationChannel
import androidx.core.app.NotificationCompat
import com.example.streetkings.core.data.UserLocation
import com.example.streetkings.core.data.WorkoutPark


class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val locationClient = DefaultLocationClient(
        application,
        LocationServices.getFusedLocationProviderClient(application)
    )

    private val parkRepository: ParkRepository = ParkRepositoryImpl()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    private val _mapState = MutableStateFlow(MapState())
    val mapState = _mapState.asStateFlow()

    private val notifiedParks = mutableSetOf<String>()
    private val notifiedUsers = mutableSetOf<String>()

    init {
        createNotificationChannel()
    }


    fun addPark(name: String, equipment: List<String>, rating: Float) {
        viewModelScope.launch {
            val user = auth.currentUser
            val currentLocation = mapState.value.currentLocation

            if (user == null || currentLocation == null) {
                // TODO: Prikazati grešku korisniku
                return@launch
            }

            val newPark = com.example.streetkings.core.data.WorkoutPark(
                name = name,
                location = GeoPoint(currentLocation.latitude, currentLocation.longitude),
                addedByUid = user.uid,
                equipment = equipment,
                rating = rating
            )

            parkRepository.addPark(newPark).collect { result ->
                result.onSuccess {
                    Log.d("ADD_PARK", "Park uspešno sačuvan!")

                    loadParks()

                    parkRepository.awardPoints(user.uid, 10).collect() { }

                }.onFailure {e ->
                    Log.e("ADD_PARK", "Greška pri čuvanju parka: ${e.message}")
                }
            }
        }
    }


    fun loadParks() {
        viewModelScope.launch {
            val equipment = mapState.value.selectedEquipment
            val rating = mapState.value.minRating
            val nameFilter = mapState.value.parkNameFilter
            val radiusKm = mapState.value.searchRadiusKm
            val currentUserLocation = mapState.value.currentLocation

            parkRepository.getParks(equipment, rating).collect { result ->
                result.onSuccess { parksFromRepo ->

                    val filteredByName = if (nameFilter.isNotBlank()) {
                        parksFromRepo.filter { park -> park.name.contains(nameFilter, ignoreCase = true) }
                    } else {
                        parksFromRepo
                    }


                    val finalFilteredParks = if (currentUserLocation != null) {
                        filteredByName.filter { park ->
                            park.location?.let { parkLocation ->
                                val distance = calculateDistance(
                                    currentUserLocation.latitude, currentUserLocation.longitude,
                                    parkLocation.latitude, parkLocation.longitude
                                )

                                distance <= radiusKm * 1000
                            } ?: false
                        }
                    } else {

                        filteredByName
                    }


                    _mapState.value = mapState.value.copy(parks = finalFilteredParks)

                }.onFailure { e -> Log.e("GET_PARKS", "ViewModel je primio grešku: ${e.message}") }
            }
        }
    }



    private fun checkForNearbyParks(currentLocation: android.location.Location) {
        // Učitaj parkove pre provere ako lista nije već učitana
        if (mapState.value.parks.isEmpty()) {
            Log.d("NEARBY_CHECK", "Lista parkova je prazna, učitavam ih...")
            loadParks()
            return // Izađi iz funkcije za sada, sačekaj sledeći update lokacije
        }

        Log.d("NEARBY_CHECK", "Proveravam blizinu za ${mapState.value.parks.size} parkova.")

        mapState.value.parks.forEach { park ->
            park.location?.let { parkLocation ->
                val distance = calculateDistance(
                    currentLocation.latitude, currentLocation.longitude,
                    parkLocation.latitude, parkLocation.longitude
                )

                Log.d("NEARBY_CHECK", "Udaljenost do parka '${park.name}' je ${distance.toInt()} metara.")

                // Proveravamo da li je park već u setu notifikacija
                val alreadyNotified = notifiedParks.contains(park.id)
                if (alreadyNotified) {
                    Log.d("NEARBY_CHECK", "Za park '${park.name}' je već poslata notifikacija, preskačem.")
                }

                if (distance < 500 && !alreadyNotified) {
                    Log.d("NEARBY_CHECK", "!!! USLOV ISPUNJEN za '${park.name}'. Šaljem notifikaciju!")
                    sendNotification("Park u blizini!", "Workout park '${park.name}' je udaljen manje od 500 metara.")
                    notifiedParks.add(park.id)
                }
            }
        }
    }


    private fun checkForNearbyUsers(currentLocation: android.location.Location) {
        mapState.value.userLocations.forEach { (userId, userLocation) ->
            val distance = calculateDistance(
                currentLocation.latitude, currentLocation.longitude,
                userLocation.latitude, userLocation.longitude
            )

            if (distance < 500 && !notifiedUsers.contains(userId)) {
                sendNotification("Korisnik u blizini!", "Drugi vežbač je u blizini.") // TODO: Dohvatiti ime korisnika
                notifiedUsers.add(userId)
            }
        }
    }


    private fun loadUserLocations() {
        auth.currentUser?.uid?.let { userId ->
            viewModelScope.launch {
                parkRepository.getUserLocations(userId).collect { result ->
                    result.onSuccess { locations ->
                        _mapState.value = mapState.value.copy(userLocations = locations)
                    }.onFailure { /* TODO */ }
                }
            }
        }
    }


    fun startLocationUpdates() {
        loadParks()
        loadUserLocations()

        locationClient.getLocationUpdates(10000L) // Svakih 10 sekundi
            .catch { e ->
                Log.e("LOCATION_ERROR", "Greška prilikom pokretanja praćenja lokacije: ${e.message}")
                e.printStackTrace()
            }
            .onEach { location ->
                // Ažuriramo stanje
                _mapState.value = mapState.value.copy(
                    currentLocation = location,
                    properties = MapProperties(
                        isMyLocationEnabled = true
                    )
                )

                // Šaljemo lokaciju na Firebase
                auth.currentUser?.uid?.let { userId ->
                    val locationData = mapOf(
                        "latitude" to location.latitude,
                        "longitude" to location.longitude
                    )
                    firestore.collection("user_locations").document(userId).set(locationData)
                        .addOnSuccessListener {
                            Log.d("LOCATION_UPLOAD", "Lokacija uspešno sačuvana.")
                        }
                        .addOnFailureListener { e ->
                            Log.w("LOCATION_UPLOAD", "Greška pri čuvanju lokacije.", e)
                        }
                }

                // Proveravamo blizinu parkova
                checkForNearbyParks(location)
                checkForNearbyUsers(location)
            }
            .launchIn(viewModelScope)
    }


    fun applyFilters(parkName: String, equipment: List<String>, rating: Float, radius: Int) {
        // 1. Ažuriraj stanje sa novim filterima
        _mapState.value = mapState.value.copy(
            parkNameFilter = parkName,
            selectedEquipment = equipment,
            minRating = rating,
            searchRadiusKm = radius
        )
        // 2. Ponovo učitaj parkove sa novim filterima
        loadParks()
    }



    private fun sendNotification(title: String, message: String) {
        val notificationManager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(getApplication(), "nearby_channel")
            .setContentTitle(title) // <-- Fali tačka
            .setContentText(message) // <-- Fali tačka
            .setSmallIcon(R.drawable.ic_launcher_foreground) // <-- Fali tačka
            .build() // <-- Fali tačka

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "nearby_channel",
                "Nearby Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


}

// Data klasa koja opisuje stanje celog MapScreen-a
data class MapState(
    val properties: MapProperties = MapProperties(),
    val currentLocation: android.location.Location? = null,
    val parks: List<WorkoutPark> = emptyList(),
    val userLocations: Map<String, UserLocation> = emptyMap(),
    val selectedEquipment: List<String> = emptyList(),
    val minRating: Float = 0f,
    val parkNameFilter: String = "",
    val searchRadiusKm: Int = 10
)