package com.example.streetkings.map.presentation

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.CameraUpdateFactory
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.padding
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import android.util.Log
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.IconButton
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.List
import com.example.streetkings.auth.presentation.Routes
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(navController: NavHostController) {
    val mapViewModel: MapViewModel = viewModel()
    val mapState by mapViewModel.mapState.collectAsState()

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
    )


    val cameraPositionState = rememberCameraPositionState {             // Kamera za mapu
        position = CameraPosition.fromLatLngZoom(LatLng(44.787197, 20.457273), 12f)
    }

    LaunchedEffect(mapState.currentLocation) {
        mapState.currentLocation?.let { location ->
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude, location.longitude),
                15f
            )

            cameraPositionState.animate(cameraUpdate)
        }
    }

    if (locationPermissionsState.allPermissionsGranted) {               //permisije za lokaciju
        LaunchedEffect(Unit) {
            mapViewModel.startLocationUpdates()
        }

        LaunchedEffect(mapState.currentLocation) {
            mapState.currentLocation?.let { location ->
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude),
                        15f
                    )
                )
            }
        }


        var showAddParkDialog by remember { mutableStateOf(false) }
        var showFilterDialog by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                properties = mapState.properties,
                cameraPositionState = cameraPositionState
            ){

                Log.d("GET_PARKS", "UI pokušava da iscrta ${mapState.parks.size} markera.")

                mapState.parks.forEach { park ->
                    park.location?.let { geoPoint ->
                        Marker(
                            state = MarkerState(position = LatLng(geoPoint.latitude, geoPoint.longitude)),
                            title = park.name,
                            snippet = "Ocena: ${park.rating.toInt()}/5 (Klikni za detalje)",
                            onInfoWindowClick = {
                                navController.navigate("park_detail/${park.id}")
                            }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 32.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Button(   //filter
                    onClick = { showFilterDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter parkova")
                }
                Spacer(modifier = Modifier.height(8.dp))


                Button(  //tabela parkova
                    onClick = { navController.navigate(Routes.PARK_TABLE_SCREEN) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.List, contentDescription = "Lista parkova")
                }
                Spacer(modifier = Modifier.height(8.dp))


                Button(  //tabela korisnika
                    onClick = { navController.navigate(Routes.LEADERBOARD_SCREEN) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Leaderboard, contentDescription = "Rang lista")
                }
            }

            FloatingActionButton(  //dodaj mapu
                onClick = { showAddParkDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(40.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj park")
            }

            if (showAddParkDialog) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AddParkScreen(
                        onAddPark = { parkName, equipment, rating ->
                            mapViewModel.addPark(parkName, equipment, rating)
                            showAddParkDialog = false
                        },
                        onCancel = {
                            showAddParkDialog = false
                        }
                    )
                }
            }

            if (showFilterDialog) {
                FilterDialog(
                    onDismiss = { showFilterDialog = false },
                    onApplyFilters = { parkName, equipment, rating, radius ->

                        mapViewModel.applyFilters(parkName, equipment, rating, radius)
                        showFilterDialog = false
                    }
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Aplikaciji je potrebna dozvola za lokaciju da bi prikazala mapu.")
            Button(onClick = {
                locationPermissionsState.launchMultiplePermissionRequest()
            }) {
                Text("Zatraži dozvole")
            }
        }
    }
}