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

    @Query("UPDATE beacon_table SET state = 1 WHERE mac = :mac")
    suspend fun setUploaded(mac: String)

    @Query("SELECT * FROM beacon_table WHERE mac = :mac")
    suspend fun getBeaconByMac(mac: String): BeaconEntity?

    @Query("SELECT * FROM beacon_table")
    fun getAllBeacons(): List<BeaconEntity>

    @Query("SELECT * FROM beacon_table WHERE state = 0")
    fun getAllCachedBeacon(): List<BeaconEntity>
}
