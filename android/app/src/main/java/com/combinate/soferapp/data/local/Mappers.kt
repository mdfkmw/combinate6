package com.combinate.soferapp.data.local

import com.combinate.soferapp.domain.model.Reservation
import com.combinate.soferapp.domain.model.ReservationStatus
import com.combinate.soferapp.domain.model.Route
import com.combinate.soferapp.domain.model.Station
import com.combinate.soferapp.domain.model.Trip
import com.combinate.soferapp.domain.model.Vehicle

fun ReservationEntity.toDomain(route: Route): Reservation {
    val fromStation = route.stations.firstOrNull { it.id == fromStationId }
        ?: Station(fromStationId, fromStationName, 0)
    val toStation = route.stations.firstOrNull { it.id == toStationId }
        ?: Station(toStationId, toStationName, 0)
    return Reservation(
        id = id,
        passengerName = passengerName,
        fromStation = fromStation,
        toStation = toStation,
        status = ReservationStatus.valueOf(status),
        tripId = tripId
    )
}

fun Reservation.toEntity(): ReservationEntity = ReservationEntity(
    id = id,
    passengerName = passengerName,
    fromStationId = fromStation.id,
    fromStationName = fromStation.name,
    toStationId = toStation.id,
    toStationName = toStation.name,
    status = status.name,
    tripId = tripId,
    timestamp = System.currentTimeMillis()
)

fun VehicleEntity.toDomain(): Vehicle = Vehicle(
    id = id,
    registrationNumber = registrationNumber,
    capacity = capacity,
    operator = operator
)

fun Vehicle.toEntity(): VehicleEntity = VehicleEntity(
    id = id,
    registrationNumber = registrationNumber,
    capacity = capacity,
    operator = operator
)

fun TripEntity.toDomain(route: Route): Trip = Trip(
    id = id,
    route = route,
    departureTime = departureTime,
    boardingStarted = boardingStarted,
    boardingClosed = boardingClosed
)

fun Trip.toEntity(): TripEntity = TripEntity(
    id = id,
    routeId = route.id,
    routeName = route.name,
    departureTime = departureTime,
    boardingStarted = boardingStarted,
    boardingClosed = boardingClosed
)

fun StationEntity.toDomain(): Station = Station(
    id = id,
    name = name,
    kilometer = kilometer
)

fun Station.toEntity(routeId: String): StationEntity = StationEntity(
    id = id,
    name = name,
    kilometer = kilometer,
    routeId = routeId
)
