package com.example.streetkings.map.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkTableScreen(
    mapViewModel: MapViewModel = viewModel(),
    onNavigateUp: () -> Unit
) {
    val mapState by mapViewModel.mapState.collectAsState()

    LaunchedEffect(Unit) {
        mapViewModel.loadParks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista Parkova") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(mapState.parks) { park ->
                ParkListItem(park = park)
            }
        }
    }
}

@Composable
fun ParkListItem(park: com.example.streetkings.core.data.WorkoutPark) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = park.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ocena: ${park.rating.toInt()}/5",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (park.equipment.isNotEmpty()) {
                Text(
                    text = "Oprema: ${park.equipment.joinToString()}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}