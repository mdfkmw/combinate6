package com.combinate.soferapp.domain.model

data class Driver(
    val id: String,
    val name: String,
    val operator: String
)

data class Vehicle(
    val id: String,
    val registrationNumber: String,
    val capacity: Int,
    val operator: String
)

data class Route(
    val id: String,
    val name: String,
    val stations: List<Station>
)

data class Station(
    val id: String,
    val name: String,
    val kilometer: Int
)

data class Trip(
    val id: String,
    val route: Route,
    val departureTime: String,
    val boardingStarted: Boolean,
    val boardingClosed: Boolean
)

data class Reservation(
    val id: String,
    val passengerName: String,
    val fromStation: Station,
    val toStation: Station,
    val status: ReservationStatus,
    val tripId: String
)

data class Ticket(
    val id: String,
    val fromStation: Station,
    val toStation: Station,
    val price: Double,
    val issuedAt: Long,
    val tripId: String
)

data class PassValidation(
    val id: String,
    val cardId: String,
    val timestamp: Long,
    val valid: Boolean,
    val tripId: String
)

data class Snapshot(
    val id: String,
    val localPath: String,
    val capturedAt: Long,
    val tripId: String
)

data class TripEvent(
    val id: String,
    val type: TripEventType,
    val createdAt: Long,
    val payload: String,
    val tripId: String
)

data class StatusBarInfo(
    val driverName: String,
    val driverId: String,
    val vehicleRegistration: String?,
    val routeName: String?,
    val departureTime: String?,
    val internetStatus: Connectivity,
    val gpsStatus: GpsStatus,
    val cashRegisterConnected: Boolean,
    val batteryLevel: Int
)

enum class ReservationStatus {
    Pending,
    Boarded,
    Cancelled
}

enum class TripEventType {
    BoardingStarted,
    BoardingStopped,
    TripFinished,
    TicketIssued,
    ReservationUpdated,
    SnapshotCaptured
}

enum class Connectivity {
    Online,
    Offline
}

enum class GpsStatus {
    Ok,
    Weak,
    Off
}
