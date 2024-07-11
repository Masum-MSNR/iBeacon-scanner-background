package dev.almasum.ibeacon_scanner_background.processor

import android.content.Context
import android.util.Log
import dev.almasum.ibeacon_scanner_background.database.local.BeaconEntity
import dev.almasum.ibeacon_scanner_background.database.local.DatabaseClient
import dev.almasum.ibeacon_scanner_background.ibeacon.BeaconModel
import dev.almasum.ibeacon_scanner_background.processor.RemoteHelper.updatePeriodic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object LocalHelper {
    fun checkDbAndInsert(
        context: Context,
        beaconModel: BeaconModel,
        type: String,
        latitude: Double,
        longitude: Double
    ) {
        if(beaconModel.latitude == 0.0 || beaconModel.longitude == 0.0) return
        CoroutineScope(Dispatchers.IO).launch {
            val db = DatabaseClient.getDatabase(context)
            val beaconDao = db.beaconDao()
            val beaconEntity = beaconDao.getBeaconByMac(beaconModel.macAddress!!)

            if (beaconEntity == null) {
                val newBeacon = BeaconEntity(
                    mac = beaconModel.macAddress!!,
                    type = type,
                    uuid = beaconModel.uuid!!,
                    latitude = latitude,
                    longitude = longitude,
                    timestamp = System.currentTimeMillis(),
                    state = 0,
                )
                try {
                    beaconDao.insert(newBeacon)
                    Log.d("DBHelper", "Beacon inserted")
                } catch (e: Exception) {
                    Log.e("DBHelper", "Error inserting beacon: ${e.message}")
                }
            } else {
                val currentTime = System.currentTimeMillis()
                if (currentTime - beaconEntity.timestamp > 300000) {
                    val newBeacon = BeaconEntity(
                        mac = beaconModel.macAddress!!,
                        type = type,
                        uuid = beaconModel.uuid!!,
                        latitude = latitude,
                        longitude = longitude,
                        timestamp = System.currentTimeMillis(),
                        state = 0,
                    )
                    try {
                        beaconDao.insert(newBeacon)
                        Log.d("DBHelper", "Beacon inserted")
                    } catch (e: Exception) {
                        Log.e("DBHelper", "Error inserting beacon: ${e.message}")
                    }
                } else {
                    Log.d("DBHelper", "Beacon ignored")
                }
            }

        }
    }

    fun getAllCachedBeacon(context: Context): List<BeaconEntity> {
        val db = DatabaseClient.getDatabase(context)
        val beaconDao = db.beaconDao()
        return beaconDao.getAllCachedBeacon()
    }


    fun updateBeacon(context: Context, id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = DatabaseClient.getDatabase(context)
            val beaconDao = db.beaconDao()
            try {
                beaconDao.updateBeacon(id)
            } catch (e: Exception) {
                Log.e("DBHelper", "Error deleting beacon: ${e.message}")
            }
            updatePeriodic(context)
        }
    }
}