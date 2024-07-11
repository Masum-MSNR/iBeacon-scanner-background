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

    @Query("SELECT * FROM beacon_table WHERE state = 0 ORDER BY timestamp ASC")
    fun getAllCachedBeacon(): List<BeaconEntity>

    @Query("UPDATE beacon_table SET state = 1 WHERE id = :id")
    suspend fun updateBeacon(id: Int)
}
