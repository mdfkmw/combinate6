package com.combinate.soferapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ReservationEntity::class,
        PendingTicketEntity::class,
        PendingPassValidationEntity::class,
        PendingSnapshotEntity::class,
        PendingTripEventEntity::class,
        VehicleEntity::class,
        TripEntity::class,
        StationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SoferDatabase : RoomDatabase() {
    abstract fun reservationDao(): ReservationDao
    abstract fun pendingTicketDao(): PendingTicketDao
    abstract fun pendingPassValidationDao(): PendingPassValidationDao
    abstract fun pendingSnapshotDao(): PendingSnapshotDao
    abstract fun pendingTripEventDao(): PendingTripEventDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun tripDao(): TripDao
    abstract fun stationDao(): StationDao
}
