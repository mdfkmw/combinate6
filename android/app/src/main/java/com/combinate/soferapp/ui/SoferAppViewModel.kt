package com.combinate.soferapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.combinate.soferapp.data.repository.SoferRepository
import com.combinate.soferapp.domain.model.Connectivity
import com.combinate.soferapp.domain.model.Driver
import com.combinate.soferapp.domain.model.GpsStatus
import com.combinate.soferapp.domain.model.PassValidation
import com.combinate.soferapp.domain.model.Reservation
import com.combinate.soferapp.domain.model.ReservationStatus
import com.combinate.soferapp.domain.model.Snapshot
import com.combinate.soferapp.domain.model.Station
import com.combinate.soferapp.domain.model.StatusBarInfo
import com.combinate.soferapp.domain.model.Ticket
import com.combinate.soferapp.domain.model.Trip
import com.combinate.soferapp.domain.model.Vehicle
import com.combinate.soferapp.domain.sync.SyncEngine
import com.combinate.soferapp.ui.state.AdminTabState
import com.combinate.soferapp.ui.state.AppStep
import com.combinate.soferapp.ui.state.AppUiState
import com.combinate.soferapp.ui.state.TripStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import java.util.UUID

class SoferAppViewModel(
    private val repository: SoferRepository,
    private val syncEngine: SyncEngine
) : ViewModel() {

    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    private var vehiclesJob: Job? = null
    private var tripsJob: Job? = null
    private var reservationsJob: Job? = null

    private val issuedTickets = mutableListOf<Ticket>()
    private var passValidationCount: Int = 0
    private var pendingSnapshotCount: Int = 0

    fun login(driverId: String) {
        if (driverId.isBlank()) {
            _state.update { it.copy(errorMessage = "Introduceți ID-ul șoferului") }
            return
        }

        viewModelScope.launch {
            val driver = repository.authenticateDriver(driverId)
            if (driver == null) {
                _state.update { it.copy(errorMessage = "Șofer inexistent") }
            } else {
                repository.refreshVehicles(driver.operator)
                observeVehicles()
                _state.update {
                    it.copy(
                        driver = driver,
                        step = AppStep.VehicleSelection,
                        errorMessage = null
                    )
                }
                updateStatusBar(driver = driver)
            }
        }
    }

    fun selectVehicle(vehicle: Vehicle) {
        val driver = _state.value.driver ?: return
        viewModelScope.launch {
            repository.refreshTrips(vehicle.id)
        }
        observeTrips()
        _state.update {
            it.copy(
                selectedVehicle = vehicle,
                step = AppStep.TripSelection,
                errorMessage = null
            )
        }
        updateStatusBar(driver = driver, vehicle = vehicle)
    }

    fun selectTrip(trip: Trip) {
        issuedTickets.clear()
        passValidationCount = 0
        pendingSnapshotCount = 0
        reservationsJob?.cancel()
        viewModelScope.launch {
            repository.refreshReservations(trip.id)
        }
        observeReservations(trip.id)
        _state.update {
            it.copy(
                selectedTrip = trip,
                step = AppStep.Dashboard,
                adminState = it.adminState.copy(
                    boardingStarted = trip.boardingStarted,
                    boardingClosed = trip.boardingClosed,
                    canStartBoarding = !trip.boardingStarted,
                    canCloseBoarding = trip.boardingStarted && !trip.boardingClosed,
                    message = null
                ),
                operationsState = it.operationsState.copy(
                    availableStations = trip.route.stations,
                    currentStation = trip.route.stations.firstOrNull(),
                    fromStation = trip.route.stations.firstOrNull(),
                    toStation = trip.route.stations.getOrNull(1),
                    calculatedPrice = calculatePrice(
                        trip.route.stations.firstOrNull(),
                        trip.route.stations.getOrNull(1)
                    ),
                    cashRegisterConnected = true
                ),
                errorMessage = null
            )
        }
        updateStatusBar(trip = trip)
    }

    fun runSync() {
        val tripId = _state.value.selectedTrip?.id
        _state.update { it.copy(adminState = it.adminState.copy(isSyncing = true, message = null)) }
        viewModelScope.launch {
            val report = syncEngine.runSync(tripId)
            if (report.snapshotsUploaded > 0) {
                pendingSnapshotCount = (pendingSnapshotCount - report.snapshotsUploaded).coerceAtLeast(0)
            }
            updateAdminState(
                _state.value.adminState.copy(
                    isSyncing = false,
                    lastSyncReport = report,
                    message = "Sincronizare completă"
                )
            )
            updateStats(_state.value.reservations)
        }
    }

    fun startBoarding() {
        val current = _state.value
        val trip = current.selectedTrip ?: return
        if (current.connectivity != Connectivity.Online) {
            updateAdminState(current.adminState.copy(message = "Necesită conexiune la internet"))
            return
        }
        viewModelScope.launch {
            val updated = repository.startBoarding(trip.id)
            updateAdminState(
                current.adminState.copy(
                    boardingStarted = true,
                    canStartBoarding = false,
                    canCloseBoarding = true,
                    message = "Îmbarcare pornită"
                )
            )
            _state.update {
                it.copy(selectedTrip = updated)
            }
            updateStatusBar(trip = updated)
        }
    }

    fun finishTrip() {
        val current = _state.value
        val trip = current.selectedTrip ?: return
        viewModelScope.launch {
            val updated = repository.finishTrip(trip.id)
            updateAdminState(
                current.adminState.copy(
                    boardingClosed = true,
                    canCloseBoarding = false,
                    message = "Cursă încheiată"
                )
            )
            _state.update { it.copy(selectedTrip = updated) }
            updateStatusBar(trip = updated)
        }
    }

    fun reinitializeCashRegister() {
        _state.update {
            it.copy(
                operationsState = it.operationsState.copy(cashRegisterConnected = true)
            )
        }
        updateStatusBar()
    }

    fun closeDay() {
        updateAdminState(_state.value.adminState.copy(message = "Z trimis la casa fiscală"))
    }

    fun setConnectivity(connectivity: Connectivity) {
        _state.update { it.copy(connectivity = connectivity) }
        updateStatusBar()
    }

    fun setGpsStatus(status: GpsStatus) {
        _state.update { it.copy(gpsStatus = status) }
        updateStatusBar()
    }

    fun setBatteryLevel(level: Int) {
        _state.update { it.copy(batteryLevel = level.coerceIn(1, 100)) }
        updateStatusBar()
    }

    fun updateCurrentStation(station: Station) {
        _state.update {
            val price = calculatePrice(station, it.operationsState.toStation)
            it.copy(
                operationsState = it.operationsState.copy(
                    currentStation = station,
                    fromStation = station,
                    calculatedPrice = price
                )
            )
        }
    }

    fun updateFromStation(station: Station) {
        _state.update {
            val price = calculatePrice(station, it.operationsState.toStation)
            it.copy(
                operationsState = it.operationsState.copy(
                    fromStation = station,
                    calculatedPrice = price
                )
            )
        }
    }

    fun updateToStation(station: Station) {
        _state.update {
            val price = calculatePrice(it.operationsState.fromStation, station)
            it.copy(
                operationsState = it.operationsState.copy(
                    toStation = station,
                    calculatedPrice = price
                )
            )
        }
    }

    fun issueTicket() {
        val current = _state.value
        val trip = current.selectedTrip ?: return
        val from = current.operationsState.fromStation ?: return
        val to = current.operationsState.toStation ?: return
        val price = current.operationsState.calculatedPrice ?: return
        if (!current.adminState.boardingStarted) {
            updateAdminState(current.adminState.copy(message = "Porniți îmbarcarea înainte de emitere"))
            return
        }
        if (!current.operationsState.cashRegisterConnected) {
            updateAdminState(current.adminState.copy(message = "Casa de marcat este deconectată"))
            return
        }
        viewModelScope.launch {
            val ticket = repository.issueTicket(trip.id, from, to, price)
            issuedTickets.add(ticket)
            updateStats(_state.value.reservations)
            updateAdminState(current.adminState.copy(message = "Bilet emis ${ticket.id}"))
        }
    }

    fun markReservationBoarded(reservation: Reservation) {
        viewModelScope.launch {
            repository.updateReservationStatus(
                reservationId = reservation.id,
                tripId = reservation.tripId,
                status = ReservationStatus.Boarded
            )
        }
    }

    fun markReservationCancelled(reservation: Reservation) {
        viewModelScope.launch {
            repository.updateReservationStatus(
                reservationId = reservation.id,
                tripId = reservation.tripId,
                status = ReservationStatus.Cancelled
            )
        }
    }

    fun recordPassValidation(cardId: String, valid: Boolean) {
        val trip = _state.value.selectedTrip ?: return
        viewModelScope.launch {
            val validation = PassValidation(
                id = UUID.randomUUID().toString(),
                cardId = cardId,
                timestamp = System.currentTimeMillis(),
                valid = valid,
                tripId = trip.id
            )
            repository.storePassValidation(validation)
            passValidationCount += 1
            updateStats(_state.value.reservations)
        }
    }

    fun captureSnapshot(localPath: String) {
        val trip = _state.value.selectedTrip ?: return
        viewModelScope.launch {
            val snapshot = Snapshot(
                id = UUID.randomUUID().toString(),
                localPath = localPath,
                capturedAt = System.currentTimeMillis(),
                tripId = trip.id
            )
            repository.storeSnapshot(snapshot)
            pendingSnapshotCount += 1
            updateStats(_state.value.reservations)
        }
    }

    fun resetMessage() {
        updateAdminState(_state.value.adminState.copy(message = null))
    }

    private fun observeVehicles() {
        vehiclesJob?.cancel()
        vehiclesJob = viewModelScope.launch {
            repository.observeVehicles().collect { vehicles ->
                _state.update { it.copy(vehicles = vehicles) }
            }
        }
    }

    private fun observeTrips() {
        tripsJob?.cancel()
        tripsJob = viewModelScope.launch {
            repository.observeTrips().collect { trips ->
                _state.update { it.copy(trips = trips) }
            }
        }
    }

    private fun observeReservations(tripId: String) {
        reservationsJob?.cancel()
        reservationsJob = viewModelScope.launch {
            repository.observeReservations(tripId).collect { reservations ->
                updateStats(reservations)
            }
        }
    }

    private fun updateStats(reservations: List<Reservation>) {
        val stats = TripStats(
            ticketsIssued = issuedTickets.size,
            passValidations = passValidationCount,
            pendingReservations = reservations.count { it.status == ReservationStatus.Pending },
            boardedReservations = reservations.count { it.status == ReservationStatus.Boarded },
            pendingSnapshots = pendingSnapshotCount,
            invalidations = reservations.count { it.status == ReservationStatus.Cancelled }
        )
        _state.update {
            it.copy(
                reservations = reservations,
                operationsState = it.operationsState.copy(
                    reservations = reservations,
                    stats = stats
                )
            )
        }
    }

    private fun updateAdminState(state: AdminTabState) {
        _state.update { it.copy(adminState = state) }
    }

    private fun updateStatusBar(
        driver: Driver? = _state.value.driver,
        vehicle: Vehicle? = _state.value.selectedVehicle,
        trip: Trip? = _state.value.selectedTrip
    ) {
        val current = _state.value
        val info = StatusBarInfo(
            driverName = driver?.name ?: "-",
            driverId = driver?.id ?: "-",
            vehicleRegistration = vehicle?.registrationNumber,
            routeName = trip?.route?.name,
            departureTime = trip?.departureTime,
            internetStatus = current.connectivity,
            gpsStatus = current.gpsStatus,
            cashRegisterConnected = current.operationsState.cashRegisterConnected,
            batteryLevel = current.batteryLevel
        )
        _state.update { it.copy(statusBar = info) }
    }

    private fun calculatePrice(from: Station?, to: Station?): Double? {
        if (from == null || to == null) return null
        val distance = (to.kilometer - from.kilometer).absoluteValue
        if (distance == 0) return null
        return 5.0 + distance * 0.45
    }
}
