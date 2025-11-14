package com.combinate.soferapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.combinate.soferapp.domain.model.Connectivity
import com.combinate.soferapp.domain.model.GpsStatus
import com.combinate.soferapp.domain.model.Reservation
import com.combinate.soferapp.domain.model.Station
import com.combinate.soferapp.ui.components.StatusBar
import com.combinate.soferapp.ui.state.AppUiState

@Composable
fun DashboardScreen(
    state: AppUiState,
    onSync: () -> Unit,
    onStartBoarding: () -> Unit,
    onFinishTrip: () -> Unit,
    onReinitializeCash: () -> Unit,
    onCloseDay: () -> Unit,
    onUpdateCurrentStation: (Station) -> Unit,
    onUpdateFromStation: (Station) -> Unit,
    onUpdateToStation: (Station) -> Unit,
    onIssueTicket: () -> Unit,
    onMarkReservationBoarded: (Reservation) -> Unit,
    onMarkReservationCancelled: (Reservation) -> Unit,
    onRecordPassValidation: (String, Boolean) -> Unit,
    onCaptureSnapshot: (String) -> Unit,
    onResetMessage: () -> Unit,
    onSetConnectivity: (Connectivity) -> Unit,
    onSetGpsStatus: (GpsStatus) -> Unit
) {
    val tabs = listOf("Administrare", "Operații")
    val (selectedTab, setSelectedTab) = remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        StatusBar(info = state.statusBar)
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { setSelectedTab(index) },
                    text = { Text(title) }
                )
            }
        }
        when (selectedTab) {
            0 -> AdministrationTab(
                state = state,
                onSync = onSync,
                onStartBoarding = onStartBoarding,
                onFinishTrip = onFinishTrip,
                onReinitializeCash = onReinitializeCash,
                onCloseDay = onCloseDay,
                onResetMessage = onResetMessage,
                onSetConnectivity = onSetConnectivity,
                onSetGpsStatus = onSetGpsStatus
            )
            else -> OperationsTab(
                state = state,
                onUpdateCurrentStation = onUpdateCurrentStation,
                onUpdateFromStation = onUpdateFromStation,
                onUpdateToStation = onUpdateToStation,
                onIssueTicket = onIssueTicket,
                onMarkReservationBoarded = onMarkReservationBoarded,
                onMarkReservationCancelled = onMarkReservationCancelled,
                onRecordPassValidation = onRecordPassValidation,
                onCaptureSnapshot = onCaptureSnapshot
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdministrationTab(
    state: AppUiState,
    onSync: () -> Unit,
    onStartBoarding: () -> Unit,
    onFinishTrip: () -> Unit,
    onReinitializeCash: () -> Unit,
    onCloseDay: () -> Unit,
    onResetMessage: () -> Unit,
    onSetConnectivity: (Connectivity) -> Unit,
    onSetGpsStatus: (GpsStatus) -> Unit
) {
    val admin = state.adminState
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Sincronizare", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onSync, enabled = !admin.isSyncing) {
                Text(text = if (admin.isSyncing) "Sincronizare..." else "Sincronizează")
            }
            OutlinedButton(onClick = onReinitializeCash) {
                Text("Reinițializează casa")
            }
        }
        admin.lastSyncReport?.let {
            Text(text = "Sincronizare: ${it.ticketsUploaded} bilete, ${it.passValidationsUploaded} abonamente, ${it.snapshotsUploaded} poze")
        }
        Text(text = "Îmbarcare", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onStartBoarding, enabled = admin.canStartBoarding) {
                Text("Pornește îmbarcare")
            }
            Button(onClick = onFinishTrip, enabled = admin.canCloseBoarding) {
                Text("Încheie cursa")
            }
            OutlinedButton(onClick = onCloseDay) {
                Text("Închide ziua")
            }
        }
        Text(text = "Stare conexiune", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = { onSetConnectivity(Connectivity.Online) }) {
                Icon(imageVector = Icons.Default.CloudQueue, contentDescription = null)
                Text("Online")
            }
            OutlinedButton(onClick = { onSetConnectivity(Connectivity.Offline) }) {
                Icon(imageVector = Icons.Default.CloudOff, contentDescription = null)
                Text("Offline")
            }
        }
        Text(text = "Stare GPS", style = MaterialTheme.typography.titleMedium)
        SegmentedButtonRow {
            SegmentedButton(
                selected = state.gpsStatus == GpsStatus.Ok,
                onClick = { onSetGpsStatus(GpsStatus.Ok) },
                label = { Text("OK") }
            )
            SegmentedButton(
                selected = state.gpsStatus == GpsStatus.Weak,
                onClick = { onSetGpsStatus(GpsStatus.Weak) },
                label = { Text("Slab") }
            )
            SegmentedButton(
                selected = state.gpsStatus == GpsStatus.Off,
                onClick = { onSetGpsStatus(GpsStatus.Off) },
                label = { Text("Off") }
            )
        }
        admin.message?.let {
            Text(text = it, fontWeight = FontWeight.SemiBold)
            OutlinedButton(onClick = onResetMessage) { Text("Închide mesaj") }
        }
    }
}

@Composable
private fun OperationsTab(
    state: AppUiState,
    onUpdateCurrentStation: (Station) -> Unit,
    onUpdateFromStation: (Station) -> Unit,
    onUpdateToStation: (Station) -> Unit,
    onIssueTicket: () -> Unit,
    onMarkReservationBoarded: (Reservation) -> Unit,
    onMarkReservationCancelled: (Reservation) -> Unit,
    onRecordPassValidation: (String, Boolean) -> Unit,
    onCaptureSnapshot: (String) -> Unit
) {
    val operations = state.operationsState
    val (cardId, setCardId) = remember { mutableStateOf("1234567890") }
    val (snapshotName, setSnapshotName) = remember { mutableStateOf("snapshot.jpg") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "Selectare stație", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Stația curentă")
                    DropdownStationSelector(
                        stations = operations.availableStations,
                        selected = operations.currentStation,
                        onSelect = onUpdateCurrentStation
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Destinație")
                    DropdownStationSelector(
                        stations = operations.availableStations,
                        selected = operations.toStation,
                        onSelect = onUpdateToStation
                    )
                }
            }
        }
        item {
            Text(text = "Emitere bilet", style = MaterialTheme.typography.titleMedium)
            Text(text = "Preț calculat: ${operations.calculatedPrice?.let { String.format("%.2f lei", it) } ?: "-"}")
            Button(onClick = onIssueTicket, enabled = operations.calculatedPrice != null) {
                Text("Emite bilet")
            }
        }
        item {
            Text(text = "Rezervări", style = MaterialTheme.typography.titleMedium)
        }
        items(operations.reservations, key = { it.id }) { reservation ->
            ReservationRow(
                reservation = reservation,
                onBoarded = { onMarkReservationBoarded(reservation) },
                onCancelled = { onMarkReservationCancelled(reservation) }
            )
        }
        item {
            Text(text = "Validări abonamente", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = cardId,
                onValueChange = setCardId,
                label = { Text("ID card") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onRecordPassValidation(cardId, true) }) {
                    Text("Validează")
                }
                OutlinedButton(onClick = { onRecordPassValidation(cardId, false) }) {
                    Text("Invalidare")
                }
            }
        }
        item {
            Text(text = "Cameră Hikvision", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = snapshotName,
                onValueChange = setSnapshotName,
                label = { Text("Nume fișier") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { onCaptureSnapshot(snapshotName) }) {
                Text("Salvează fotografie")
            }
        }
        item {
            Text(text = "Statistici", style = MaterialTheme.typography.titleMedium)
            Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Bilete emise: ${operations.stats.ticketsIssued}")
                    Text(text = "Validări abonamente: ${operations.stats.passValidations}")
                    Text(text = "Rezervări în așteptare: ${operations.stats.pendingReservations}")
                    Text(text = "Rezervări îmbarcate: ${operations.stats.boardedReservations}")
                    Text(text = "Fotografii pending: ${operations.stats.pendingSnapshots}")
                    Text(text = "Invalidări: ${operations.stats.invalidations}")
                }
            }
        }
    }
}

@Composable
private fun DropdownStationSelector(
    stations: List<Station>,
    selected: Station?,
    onSelect: (Station) -> Unit
) {
    val expandedState = remember { mutableStateOf(false) }
    Column {
        OutlinedButton(onClick = { expandedState.value = !expandedState.value }) {
            Text(text = selected?.name ?: "Selectează")
        }
        if (expandedState.value) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                stations.forEach { station ->
                    OutlinedButton(onClick = {
                        onSelect(station)
                        expandedState.value = false
                    }) {
                        Text(station.name)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationRow(
    reservation: Reservation,
    onBoarded: () -> Unit,
    onCancelled: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "${reservation.passengerName}: ${reservation.fromStation.name} → ${reservation.toStation.name}")
            Text(text = "Status: ${reservation.status}")
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onBoarded) { Text("Îmbarcat") }
                OutlinedButton(onClick = onCancelled) { Text("Anulează") }
            }
        }
    }
}
