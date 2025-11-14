package com.combinate.soferapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey val id: String,
    val passengerName: String,
    val fromStationId: String,
    val fromStationName: String,
    val toStationId: String,
    val toStationName: String,
    val status: String,
    val tripId: String,
    val timestamp: Long
)

@Entity(tableName = "pending_tickets")
data class PendingTicketEntity(
    @PrimaryKey val id: String,
    val fromStationId: String,
    val fromStationName: String,
    val toStationId: String,
    val toStationName: String,
    val price: Double,
    val issuedAt: Long,
    val tripId: String
)

@Entity(tableName = "pending_pass_validations")
data class PendingPassValidationEntity(
    @PrimaryKey val id: String,
    val cardId: String,
    val timestamp: Long,
    val valid: Boolean,
    val tripId: String
)

@Entity(tableName = "pending_snapshots")
data class PendingSnapshotEntity(
    @PrimaryKey val id: String,
    val localPath: String,
    val capturedAt: Long,
    val tripId: String
)

@Entity(tableName = "pending_trip_events")
data class PendingTripEventEntity(
    @PrimaryKey val id: String,
    val type: String,
    val createdAt: Long,
    val payload: String,
    val tripId: String
)

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: String,
    val registrationNumber: String,
    val capacity: Int,
    val operator: String
)

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val routeId: String,
    val routeName: String,
    val departureTime: String,
    val boardingStarted: Boolean,
    val boardingClosed: Boolean
)

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val kilometer: Int,
    val routeId: String
)
