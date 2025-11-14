package com.combinate.soferapp.data.repository

import com.combinate.soferapp.data.local.PendingPassValidationDao
import com.combinate.soferapp.data.local.PendingPassValidationEntity
import com.combinate.soferapp.data.local.PendingSnapshotDao
import com.combinate.soferapp.data.local.PendingSnapshotEntity
import com.combinate.soferapp.data.local.PendingTicketDao
import com.combinate.soferapp.data.local.PendingTicketEntity
import com.combinate.soferapp.data.local.PendingTripEventDao
import com.combinate.soferapp.data.local.PendingTripEventEntity
import com.combinate.soferapp.data.local.ReservationDao
import com.combinate.soferapp.data.local.SoferDatabase
import com.combinate.soferapp.data.local.StationDao
import com.combinate.soferapp.data.local.TripDao
import com.combinate.soferapp.data.local.VehicleDao
import com.combinate.soferapp.data.local.toDomain
import com.combinate.soferapp.data.local.toEntity
import com.combinate.soferapp.domain.model.Driver
import com.combinate.soferapp.domain.model.PassValidation
import com.combinate.soferapp.domain.model.Reservation
import com.combinate.soferapp.domain.model.ReservationStatus
import com.combinate.soferapp.domain.model.Route
import com.combinate.soferapp.domain.model.Snapshot
import com.combinate.soferapp.domain.model.Station
import com.combinate.soferapp.domain.model.Ticket
import com.combinate.soferapp.domain.model.Trip
import com.combinate.soferapp.domain.model.TripEvent
import com.combinate.soferapp.domain.model.TripEventType
import com.combinate.soferapp.domain.model.Vehicle
import com.combinate.soferapp.data.remote.BackendApi
import com.combinate.soferapp.data.local.TripEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class SoferRepository(
    private val backendApi: BackendApi,
    private val database: SoferDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val reservationDao: ReservationDao = database.reservationDao()
    private val pendingTicketDao: PendingTicketDao = database.pendingTicketDao()
    private val pendingPassValidationDao: PendingPassValidationDao = database.pendingPassValidationDao()
    private val pendingSnapshotDao: PendingSnapshotDao = database.pendingSnapshotDao()
    private val pendingTripEventDao: PendingTripEventDao = database.pendingTripEventDao()
    private val vehicleDao: VehicleDao = database.vehicleDao()
    private val tripDao: TripDao = database.tripDao()
    private val stationDao: StationDao = database.stationDao()

    suspend fun authenticateDriver(id: String): Driver? = backendApi.authenticateDriver(id)

    fun observeVehicles(): Flow<List<Vehicle>> =
        vehicleDao.observeVehicles().map { entities -> entities.map { it.toDomain() } }

    suspend fun refreshVehicles(operator: String) = withContext(ioDispatcher) {
        val vehicles = backendApi.fetchVehicles(operator)
        vehicleDao.upsertAll(vehicles.map { it.toEntity() })
    }

    fun observeTrips(): Flow<List<Trip>> = tripDao.observeTrips().map { trips ->
        trips.map { it.toDomain(buildRoute(it)) }
    }

    suspend fun refreshTrips(vehicleId: String) = withContext(ioDispatcher) {
        val trips = backendApi.fetchTrips(vehicleId)
        tripDao.clear()
        stationDao.clearAll()
        tripDao.upsertAll(trips.map { it.toEntity() })
        trips.forEach { trip ->
            stationDao.upsertAll(trip.route.stations.map { it.toEntity(trip.route.id) })
        }
    }

    fun observeReservations(tripId: String): Flow<List<Reservation>> =
        reservationDao.observeReservations(tripId).map { entities ->
            val route = buildRouteForTrip(tripId)
            entities.map { it.toDomain(route) }
        }

    suspend fun refreshReservations(tripId: String) = withContext(ioDispatcher) {
        val remoteReservations = backendApi.fetchReservations(tripId)
        reservationDao.clearTrip(tripId)
        reservationDao.upsertAll(remoteReservations.map { it.toEntity() })
    }

    suspend fun startBoarding(tripId: String): Trip = withContext(ioDispatcher) {
        val remote = backendApi.startBoarding(tripId)
        tripDao.updateBoarding(tripId, true)
        tripDao.upsertAll(listOf(remote.toEntity()))
        pendingTripEventDao.upsert(
            PendingTripEventEntity(
                id = UUID.randomUUID().toString(),
                type = TripEventType.BoardingStarted.name,
                createdAt = System.currentTimeMillis(),
                payload = "Boarding started",
                tripId = tripId
            )
        )
        remote
    }

    suspend fun finishTrip(tripId: String): Trip = withContext(ioDispatcher) {
        val remote = backendApi.finishTrip(tripId)
        tripDao.updateBoardingClosed(tripId, true)
        pendingTripEventDao.upsert(
            PendingTripEventEntity(
                id = UUID.randomUUID().toString(),
                type = TripEventType.TripFinished.name,
                createdAt = System.currentTimeMillis(),
                payload = "Trip finished",
                tripId = tripId
            )
        )
        remote
    }

    suspend fun issueTicket(
        tripId: String,
        from: Station,
        to: Station,
        price: Double
    ): Ticket = withContext(ioDispatcher) {
        val ticket = Ticket(
            id = UUID.randomUUID().toString(),
            fromStation = from,
            toStation = to,
            price = price,
            issuedAt = System.currentTimeMillis(),
            tripId = tripId
        )
        pendingTicketDao.upsert(
            PendingTicketEntity(
                id = ticket.id,
                fromStationId = from.id,
                fromStationName = from.name,
                toStationId = to.id,
                toStationName = to.name,
                price = price,
                issuedAt = ticket.issuedAt,
                tripId = tripId
            )
        )
        pendingTripEventDao.upsert(
            PendingTripEventEntity(
                id = UUID.randomUUID().toString(),
                type = TripEventType.TicketIssued.name,
                createdAt = System.currentTimeMillis(),
                payload = "Ticket ${ticket.id}",
                tripId = tripId
            )
        )
        ticket
    }

    suspend fun addReservation(reservation: Reservation) = withContext(ioDispatcher) {
        reservationDao.upsertAll(listOf(reservation.toEntity()))
    }

    suspend fun updateReservationStatus(reservationId: String, tripId: String, status: ReservationStatus) =
        withContext(ioDispatcher) {
            val current = reservationDao.getReservations(tripId).firstOrNull { it.id == reservationId }
                ?: return@withContext
            reservationDao.upsertAll(
                listOf(
                    current.copy(
                        status = status.name,
                        timestamp = System.currentTimeMillis()
                    )
                )
            )
            pendingTripEventDao.upsert(
                PendingTripEventEntity(
                    id = UUID.randomUUID().toString(),
                    type = TripEventType.ReservationUpdated.name,
                    createdAt = System.currentTimeMillis(),
                    payload = "Reservation $reservationId -> ${status.name}",
                    tripId = tripId
                )
            )
        }

    suspend fun storePassValidation(validation: PassValidation) = withContext(ioDispatcher) {
        pendingPassValidationDao.upsert(
            PendingPassValidationEntity(
                id = validation.id,
                cardId = validation.cardId,
                timestamp = validation.timestamp,
                valid = validation.valid,
                tripId = validation.tripId
            )
        )
    }

    suspend fun storeSnapshot(snapshot: Snapshot) = withContext(ioDispatcher) {
        pendingSnapshotDao.upsert(
            PendingSnapshotEntity(
                id = snapshot.id,
                localPath = snapshot.localPath,
                capturedAt = snapshot.capturedAt,
                tripId = snapshot.tripId
            )
        )
        pendingTripEventDao.upsert(
            PendingTripEventEntity(
                id = UUID.randomUUID().toString(),
                type = TripEventType.SnapshotCaptured.name,
                createdAt = System.currentTimeMillis(),
                payload = snapshot.localPath,
                tripId = snapshot.tripId
            )
        )
    }

    suspend fun collectPendingTickets(): List<Ticket> = withContext(ioDispatcher) {
        pendingTicketDao.getAll().map {
            Ticket(
                id = it.id,
                fromStation = Station(it.fromStationId, it.fromStationName, 0),
                toStation = Station(it.toStationId, it.toStationName, 0),
                price = it.price,
                issuedAt = it.issuedAt,
                tripId = it.tripId
            )
        }
    }

    suspend fun removeTicket(id: String) = withContext(ioDispatcher) {
        pendingTicketDao.delete(id)
    }

    suspend fun collectPendingPassValidations(): List<PassValidation> = withContext(ioDispatcher) {
        pendingPassValidationDao.getAll().map {
            PassValidation(
                id = it.id,
                cardId = it.cardId,
                timestamp = it.timestamp,
                valid = it.valid,
                tripId = it.tripId
            )
        }
    }

    suspend fun removePassValidation(id: String) = withContext(ioDispatcher) {
        pendingPassValidationDao.delete(id)
    }

    suspend fun collectPendingSnapshots(): List<Snapshot> = withContext(ioDispatcher) {
        pendingSnapshotDao.getAll().map {
            Snapshot(
                id = it.id,
                localPath = it.localPath,
                capturedAt = it.capturedAt,
                tripId = it.tripId
            )
        }
    }

    suspend fun removeSnapshot(id: String) = withContext(ioDispatcher) {
        pendingSnapshotDao.delete(id)
    }

    suspend fun collectPendingEvents(): List<TripEvent> = withContext(ioDispatcher) {
        pendingTripEventDao.getAll().map {
            TripEvent(
                id = it.id,
                type = TripEventType.valueOf(it.type),
                createdAt = it.createdAt,
                payload = it.payload,
                tripId = it.tripId
            )
        }
    }

    suspend fun removeEvent(id: String) = withContext(ioDispatcher) {
        pendingTripEventDao.delete(id)
    }

    private suspend fun buildRoute(entity: TripEntity): Route {
        val stations = stationDao.getStations(entity.routeId).map { it.toDomain() }
        return Route(
            id = entity.routeId,
            name = entity.routeName,
            stations = stations
        )
    }

    private suspend fun buildRouteForTrip(tripId: String): Route {
        val entity = tripDao.getTrip(tripId) ?: return Route(tripId, "Necunoscut", emptyList())
        return buildRoute(entity)
    }
}
