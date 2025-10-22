package com.example.streetkings.map.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkDetailScreen(
    navController: NavController,
    parkDetailViewModel: ParkDetailViewModel = viewModel()
) {
    val state by parkDetailViewModel.parkState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalji Parka") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val currentState = state) {
                is ParkDetailState.Loading -> CircularProgressIndicator()
                is ParkDetailState.Error -> Text(text = currentState.message)
                is ParkDetailState.Success -> {
                    val park = currentState.park

                    var userRating by remember { mutableStateOf(0) }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(text = park.name, style = MaterialTheme.typography.headlineLarge)
                            Spacer(modifier = Modifier.height(16.dp))


                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Ostavi svoju ocenu:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))


                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                RatingBar(
                                    rating = userRating,
                                    onRatingChanged = { newRating -> userRating = newRating }
                                )
                            }


                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { parkDetailViewModel.submitRating(userRating) },
                                enabled = userRating > 0,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Pošalji ocenu")
                            }
                        }
                    }
                }
            }
        }
    }
}