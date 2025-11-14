package com.combinate.soferapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.combinate.soferapp.domain.model.Connectivity
import com.combinate.soferapp.domain.model.GpsStatus
import com.combinate.soferapp.domain.model.StatusBarInfo

@Composable
fun StatusBar(info: StatusBarInfo?, modifier: Modifier = Modifier) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = modifier) {
        if (info == null) {
            Text(
                text = "Fără informații",
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            return@Surface
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Șofer ${info.driverId} - ${info.driverName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            info.vehicleRegistration?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsBus, contentDescription = null)
                    Text(text = it, modifier = Modifier.padding(start = 4.dp))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NetworkCheck, contentDescription = null)
                Text(
                    text = if (info.internetStatus == Connectivity.Online) "Online" else "Offline",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GpsFixed, contentDescription = null)
                Text(text = info.gpsStatus.toDisplay(), modifier = Modifier.padding(start = 4.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BatteryFull, contentDescription = null)
                Text(text = "${info.batteryLevel}%", modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}

private fun GpsStatus.toDisplay(): String = when (this) {
    GpsStatus.Ok -> "GPS OK"
    GpsStatus.Weak -> "GPS slab"
    GpsStatus.Off -> "GPS off"
}
