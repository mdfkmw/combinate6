package com.combinate.soferapp.domain.sync

import com.combinate.soferapp.data.remote.BackendApi
import com.combinate.soferapp.data.repository.SoferRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SyncReport(
    val ticketsUploaded: Int,
    val passValidationsUploaded: Int,
    val snapshotsUploaded: Int,
    val eventsUploaded: Int
)

class SyncEngine(
    private val repository: SoferRepository,
    private val backendApi: BackendApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun runSync(currentTripId: String?): SyncReport = withContext(dispatcher) {
        val tickets = repository.collectPendingTickets()
        if (tickets.isNotEmpty()) {
            backendApi.uploadTickets(tickets)
            tickets.forEach { repository.removeTicket(it.id) }
        }

        val validations = repository.collectPendingPassValidations()
        if (validations.isNotEmpty()) {
            backendApi.uploadPassValidations(validations)
            validations.forEach { repository.removePassValidation(it.id) }
        }

        val snapshots = repository.collectPendingSnapshots()
        if (snapshots.isNotEmpty()) {
            backendApi.uploadSnapshots(snapshots)
            snapshots.forEach { repository.removeSnapshot(it.id) }
        }

        val events = repository.collectPendingEvents()
        if (events.isNotEmpty()) {
            backendApi.uploadTripEvents(events)
            events.forEach { repository.removeEvent(it.id) }
        }

        currentTripId?.let { repository.refreshReservations(it) }

        SyncReport(
            ticketsUploaded = tickets.size,
            passValidationsUploaded = validations.size,
            snapshotsUploaded = snapshots.size,
            eventsUploaded = events.size
        )
    }
}
