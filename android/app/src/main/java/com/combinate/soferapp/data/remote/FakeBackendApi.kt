package com.combinate.soferapp.data.remote

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
import com.combinate.soferapp.domain.model.Vehicle
import kotlinx.coroutines.delay

class FakeBackendApi : BackendApi {
    private val drivers = listOf(
        Driver(id = "1001", name = "Ion Popescu", operator = "NordTrans"),
        Driver(id = "1002", name = "Maria Ionescu", operator = "NordTrans"),
        Driver(id = "2001", name = "George Matei", operator = "SudExpress")
    )

    private val routes = listOf(
        Route(
            id = "r1",
            name = "Cluj - Oradea",
            stations = listOf(
                Station("clj", "Cluj Napoca", 0),
                Station("afm", "Aghireșu", 35),
                Station("hsm", "Huedin", 55),
                Station("orl", "Oradea", 160)
            )
        ),
        Route(
            id = "r2",
            name = "Cluj - Alba Iulia",
            stations = listOf(
                Station("clj", "Cluj Napoca", 0),
                Station("tur", "Turda", 40),
                Station("ai", "Alba Iulia", 97)
            )
        )
    )

    private val vehicles = listOf(
        Vehicle(id = "v1", registrationNumber = "CJ-01-AAA", capacity = 50, operator = "NordTrans"),
        Vehicle(id = "v2", registrationNumber = "CJ-02-BBB", capacity = 48, operator = "NordTrans"),
        Vehicle(id = "v3", registrationNumber = "B-99-XYZ", capacity = 52, operator = "SudExpress")
    )

    private val trips = mutableMapOf<String, Trip>().apply {
        put(
            "t1",
            Trip(
                id = "t1",
                route = routes[0],
                departureTime = "07:00",
                boardingStarted = false,
                boardingClosed = false
            )
        )
        put(
            "t2",
            Trip(
                id = "t2",
                route = routes[1],
                departureTime = "08:30",
                boardingStarted = false,
                boardingClosed = false
            )
        )
    }

    private val reservations = mutableMapOf(
        "t1" to mutableListOf(
            Reservation(
                id = "res1",
                passengerName = "Andrei",
                fromStation = routes[0].stations[0],
                toStation = routes[0].stations[3],
                status = ReservationStatus.Pending,
                tripId = "t1"
            ),
            Reservation(
                id = "res2",
                passengerName = "Bianca",
                fromStation = routes[0].stations[1],
                toStation = routes[0].stations[2],
                status = ReservationStatus.Pending,
                tripId = "t1"
            )
        ),
        "t2" to mutableListOf(
            Reservation(
                id = "res3",
                passengerName = "Claudiu",
                fromStation = routes[1].stations[0],
                toStation = routes[1].stations[2],
                status = ReservationStatus.Pending,
                tripId = "t2"
            )
        )
    )

    override suspend fun authenticateDriver(id: String): Driver? {
        delay(250)
        return drivers.firstOrNull { it.id == id }
    }

    override suspend fun fetchVehicles(operator: String): List<Vehicle> {
        delay(250)
        return vehicles.filter { it.operator == operator }
    }

    override suspend fun fetchTrips(vehicleId: String): List<Trip> {
        delay(250)
        return when (vehicleId) {
            "v1" -> listOfNotNull(trips["t1"], trips["t2"])
            "v2" -> listOfNotNull(trips["t2"])
            "v3" -> listOf(
                Trip(
                    id = "t3",
                    route = routes[0],
                    departureTime = "10:15",
                    boardingStarted = false,
                    boardingClosed = false
                )
            )
            else -> emptyList()
        }
    }

    override suspend fun fetchReservations(tripId: String): List<Reservation> {
        delay(250)
        return reservations[tripId]?.toList() ?: emptyList()
    }

    override suspend fun uploadTickets(tickets: List<Ticket>) {
        delay(150)
    }

    override suspend fun uploadPassValidations(validations: List<PassValidation>) {
        delay(150)
    }

    override suspend fun uploadSnapshots(snapshots: List<Snapshot>) {
        delay(150)
    }

    override suspend fun uploadTripEvents(events: List<TripEvent>) {
        delay(150)
    }

    override suspend fun startBoarding(tripId: String): Trip {
        delay(150)
        val updated = trips.getValue(tripId).copy(boardingStarted = true)
        trips[tripId] = updated
        return updated
    }

    override suspend fun finishTrip(tripId: String): Trip {
        delay(150)
        val updated = trips.getValue(tripId).copy(boardingClosed = true)
        trips[tripId] = updated
        reservations.remove(tripId)
        return updated
    }
}
