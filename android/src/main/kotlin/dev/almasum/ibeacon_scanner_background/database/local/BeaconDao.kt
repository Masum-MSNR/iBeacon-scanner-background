package dev.almasum.ibeacon_scanner_background.database.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BeaconDao {
    @Insert
    suspend fun insert(beaconEntity: BeaconEntity)

    @Update
    suspend fun update(beaconEntity: BeaconEntity)

    @Query("SELECT * FROM beacon_table WHERE mac = :mac ORDER BY timestamp DESC LIMIT 1")
    suspend fun getBeaconByMac(mac: String): BeaconEntity?

    @Query("SELECT * FROM beacon_table")
    fun getAllCachedBeacon(): List<BeaconEntity>

    @Query("DELETE FROM beacon_table WHERE id = :id")
    suspend fun deleteBeacon(id: Int)
}
