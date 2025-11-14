package com.combinate.soferapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.combinate.soferapp.domain.model.Vehicle

@Composable
fun VehicleSelectionScreen(
    vehicles: List<Vehicle>,
    onVehicleSelected: (Vehicle) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Selectează vehiculul", modifier = Modifier.padding(bottom = 12.dp))
            LazyColumn {
                items(vehicles) { vehicle ->
                    Card(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .clickable { onVehicleSelected(vehicle) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = vehicle.registrationNumber)
                            Text(text = "Capacitate ${vehicle.capacity}")
                        }
                    }
                }
            }
        }
    }
}
