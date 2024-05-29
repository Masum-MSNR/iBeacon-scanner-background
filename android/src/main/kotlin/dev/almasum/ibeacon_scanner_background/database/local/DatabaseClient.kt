package dev.almasum.ibeacon_scanner_background.database.local

import android.content.Context
import androidx.room.Room

object DatabaseClient {
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "inv_app"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}
