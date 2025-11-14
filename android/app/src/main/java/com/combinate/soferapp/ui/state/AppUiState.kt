package com.combinate.soferapp.ui.state

import com.combinate.soferapp.domain.model.Driver
import com.combinate.soferapp.domain.model.Reservation
import com.combinate.soferapp.domain.model.Station
import com.combinate.soferapp.domain.model.StatusBarInfo
import com.combinate.soferapp.domain.model.Trip
import com.combinate.soferapp.domain.model.Vehicle
import com.combinate.soferapp.domain.model.Connectivity
import com.combinate.soferapp.domain.model.GpsStatus
import com.combinate.soferapp.domain.sync.SyncReport

enum class AppStep {
    Login,
    VehicleSelection,
    TripSelection,
    Dashboard
}

data class AdminTabState(
    val isSyncing: Boolean = false,
    val lastSyncReport: SyncReport? = null,
    val boardingStarted: Boolean = false,
    val boardingClosed: Boolean = false,
    val canStartBoarding: Boolean = false,
    val canCloseBoarding: Boolean = false,
    val message: String? = null
)

data class TripStats(
    val ticketsIssued: Int = 0,
    val passValidations: Int = 0,
    val pendingReservations: Int = 0,
    val boardedReservations: Int = 0,
    val pendingSnapshots: Int = 0,
    val invalidations: Int = 0
)

data class OperationsTabState(
    val availableStations: List<Station> = emptyList(),
    val currentStation: Station? = null,
    val fromStation: Station? = null,
    val toStation: Station? = null,
    val calculatedPrice: Double? = null,
    val reservations: List<Reservation> = emptyList(),
    val stats: TripStats = TripStats(),
    val cashRegisterConnected: Boolean = false,
    val nfcEnabled: Boolean = true
)

data class AppUiState(
    val step: AppStep = AppStep.Login,
    val driver: Driver? = null,
    val vehicles: List<Vehicle> = emptyList(),
    val selectedVehicle: Vehicle? = null,
    val trips: List<Trip> = emptyList(),
    val selectedTrip: Trip? = null,
    val reservations: List<Reservation> = emptyList(),
    val statusBar: StatusBarInfo? = null,
    val adminState: AdminTabState = AdminTabState(),
    val operationsState: OperationsTabState = OperationsTabState(),
    val connectivity: Connectivity = Connectivity.Online,
    val gpsStatus: GpsStatus = GpsStatus.Ok,
    val batteryLevel: Int = 85,
    val errorMessage: String? = null
)
