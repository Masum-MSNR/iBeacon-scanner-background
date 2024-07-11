package dev.almasum.ibeacon_scanner_background.database.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "beacon_table")
data class BeaconEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mac: String,
    val type: String,
    val uuid: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
//    val state: Int
)
