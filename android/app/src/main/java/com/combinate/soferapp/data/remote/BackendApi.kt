package com.combinate.soferapp.data.remote

import com.combinate.soferapp.domain.model.Driver
import com.combinate.soferapp.domain.model.PassValidation
import com.combinate.soferapp.domain.model.Reservation
import com.combinate.soferapp.domain.model.Snapshot
import com.combinate.soferapp.domain.model.Ticket
import com.combinate.soferapp.domain.model.Trip
import com.combinate.soferapp.domain.model.Vehicle

interface BackendApi {
    suspend fun authenticateDriver(id: String): Driver?
    suspend fun fetchVehicles(operator: String): List<Vehicle>
    suspend fun fetchTrips(vehicleId: String): List<Trip>
    suspend fun fetchReservations(tripId: String): List<Reservation>
    suspend fun uploadTickets(tickets: List<Ticket>)
    suspend fun uploadPassValidations(validations: List<PassValidation>)
    suspend fun uploadSnapshots(snapshots: List<Snapshot>)
    suspend fun uploadTripEvents(events: List<com.combinate.soferapp.domain.model.TripEvent>)
    suspend fun startBoarding(tripId: String): Trip
    suspend fun finishTrip(tripId: String): Trip
}
