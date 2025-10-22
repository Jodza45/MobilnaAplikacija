package com.example.streetkings.map.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun AddParkScreen(
    onAddPark: (String, List<String>, Float) -> Unit,
    onCancel: () -> Unit
) {
    var parkName by remember { mutableStateOf("") }
    val selectedEquipment = remember { mutableStateListOf<String>() }
    var rating by remember { mutableStateOf(3f) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Dodaj novi workout park", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = parkName,
                onValueChange = { parkName = it },
                label = { Text("Ime parka") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Oprema:", style = MaterialTheme.typography.titleMedium)

            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                items(allEquipmentOptions) { equipment ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = equipment in selectedEquipment,
                            onCheckedChange = {
                                if (it) {
                                    selectedEquipment.add(equipment)
                                } else {
                                    selectedEquipment.remove(equipment)
                                }
                            }
                        )
                        Text(text = equipment)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Ocena kvaliteta (1-5): ${rating.roundToInt()}", style = MaterialTheme.typography.titleMedium)

            Slider(
                value = rating,
                onValueChange = { rating = it },
                valueRange = 1f..5f,
                steps = 3
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onCancel) {
                    Text("Otkaži")
                }
                Button(
                    onClick = {
                        if (parkName.isNotBlank()) {
                            onAddPark(parkName, selectedEquipment.toList(), rating)
                        }
                    },
                    enabled = parkName.isNotBlank()
                ) {
                    Text("Dodaj park")
                }
            }
        }
    }
}