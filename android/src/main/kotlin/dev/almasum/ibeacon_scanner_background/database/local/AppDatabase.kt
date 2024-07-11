package dev.almasum.ibeacon_scanner_background.database.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BeaconEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun beaconDao(): BeaconDao
}
