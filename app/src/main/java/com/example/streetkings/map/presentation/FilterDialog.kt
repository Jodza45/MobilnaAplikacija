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
import androidx.compose.material3.OutlinedTextField

@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onApplyFilters: (String, List<String>, Float, Int) -> Unit
) {
    var parkNameFilter by remember { mutableStateOf("") }
    val selectedEquipment = remember { mutableStateListOf<String>() }
    var minRating by remember { mutableStateOf(0f) }
    var radiusInKm by remember { mutableStateOf(10) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Parkova") },
        text = {
            Column {
                OutlinedTextField(
                    value = parkNameFilter,
                    onValueChange = { parkNameFilter = it },
                    label = { Text("Pretraga po imenu") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Radijus pretrage: ${radiusInKm} km")
                Slider(
                    value = radiusInKm.toFloat(),
                    onValueChange = { radiusInKm = it.toInt() },
                    valueRange = 1f..10f,
                    steps = 8
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Minimalna ocena: ${minRating.roundToInt()}")
                Slider(
                    value = minRating,
                    onValueChange = { minRating = it },
                    valueRange = 0f..5f,
                    steps = 4
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Oprema:")
                LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                    items(allEquipmentOptions) { equipment ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = equipment in selectedEquipment,
                                onCheckedChange = {
                                    if (it) selectedEquipment.add(equipment)
                                    else selectedEquipment.remove(equipment)
                                }
                            )
                            Text(equipment)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onApplyFilters(parkNameFilter, selectedEquipment.toList(), minRating,radiusInKm) }) {
                Text("Primeni")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Poništi")
            }
        }
    )
}