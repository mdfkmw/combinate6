package com.combinate.soferapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {
    @Query("SELECT * FROM reservations WHERE tripId = :tripId")
    fun observeReservations(tripId: String): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE tripId = :tripId")
    suspend fun getReservations(tripId: String): List<ReservationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(reservations: List<ReservationEntity>)

    @Query("DELETE FROM reservations WHERE tripId = :tripId")
    suspend fun clearTrip(tripId: String)
}

@Dao
interface PendingTicketDao {
    @Query("SELECT * FROM pending_tickets")
    suspend fun getAll(): List<PendingTicketEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(ticket: PendingTicketEntity)

    @Query("DELETE FROM pending_tickets WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM pending_tickets")
    suspend fun clear()
}

@Dao
interface PendingPassValidationDao {
    @Query("SELECT * FROM pending_pass_validations")
    suspend fun getAll(): List<PendingPassValidationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PendingPassValidationEntity)

    @Query("DELETE FROM pending_pass_validations WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM pending_pass_validations")
    suspend fun clear()
}

@Dao
interface PendingSnapshotDao {
    @Query("SELECT * FROM pending_snapshots")
    suspend fun getAll(): List<PendingSnapshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PendingSnapshotEntity)

    @Query("DELETE FROM pending_snapshots WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM pending_snapshots")
    suspend fun clear()
}

@Dao
interface PendingTripEventDao {
    @Query("SELECT * FROM pending_trip_events")
    suspend fun getAll(): List<PendingTripEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PendingTripEventEntity)

    @Query("DELETE FROM pending_trip_events WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM pending_trip_events")
    suspend fun clear()
}

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles")
    fun observeVehicles(): Flow<List<VehicleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(vehicles: List<VehicleEntity>)

    @Query("DELETE FROM vehicles")
    suspend fun clear()
}

@Dao
interface TripDao {
    @Query("SELECT * FROM trips")
    fun observeTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    suspend fun getTrip(tripId: String): TripEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(trips: List<TripEntity>)

    @Query("DELETE FROM trips")
    suspend fun clear()

    @Query("UPDATE trips SET boardingStarted = :boardingStarted WHERE id = :tripId")
    suspend fun updateBoarding(tripId: String, boardingStarted: Boolean)

    @Query("UPDATE trips SET boardingClosed = :boardingClosed WHERE id = :tripId")
    suspend fun updateBoardingClosed(tripId: String, boardingClosed: Boolean)
}

@Dao
interface StationDao {
    @Query("SELECT * FROM stations WHERE routeId = :routeId")
    suspend fun getStations(routeId: String): List<StationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(stations: List<StationEntity>)

    @Query("DELETE FROM stations WHERE routeId = :routeId")
    suspend fun clear(routeId: String)

    @Query("DELETE FROM stations")
    suspend fun clearAll()
}
