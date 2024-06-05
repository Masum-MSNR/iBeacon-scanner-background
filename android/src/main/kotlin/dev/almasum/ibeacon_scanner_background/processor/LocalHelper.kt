package dev.almasum.ibeacon_scanner_background.processor

import android.content.Context
import android.util.Log
import dev.almasum.ibeacon_scanner_background.database.local.BeaconEntity
import dev.almasum.ibeacon_scanner_background.database.local.DatabaseClient
import dev.almasum.ibeacon_scanner_background.ibeacon.BeaconModel
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
        CoroutineScope(Dispatchers.IO).launch {
            val db = DatabaseClient.getDatabase(context)

            val beaconDao = db.beaconDao()
            val beaconEntity = beaconDao.getBeaconByMac(beaconModel.macAddress!!)

            if (beaconEntity == null) {
                val newBeacon = BeaconEntity(
                    beaconModel.macAddress!!,
                    type,
                    beaconModel.uuid!!,
                    latitude,
                    longitude,
                    System.currentTimeMillis(),
                    0
                )
                try {
                    beaconDao.insert(newBeacon)
                } catch (e: Exception) {
                    Log.e("DBHelper", "Error inserting beacon: ${e.message}")
                }
                Log.d("DBHelper", "Beacon inserted")
            } else {
                val currentTime = System.currentTimeMillis()
                if (currentTime - beaconEntity.timestamp > 300000 || beaconEntity.state == 0) {
                    val newBeacon = BeaconEntity(
                        beaconModel.macAddress!!,
                        type,
                        beaconModel.uuid!!,
                        latitude,
                        longitude,
                        System.currentTimeMillis(),
                        0
                    )
                    try {
                        beaconDao.update(newBeacon)
                    } catch (e: Exception) {
                        Log.e("DBHelper", "Error updating beacon: ${e.message}")
                    }
                    Log.d("DBHelper", "Beacon updated")
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

    fun setUploaded(context: Context, mac: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = DatabaseClient.getDatabase(context)
            val beaconDao = db.beaconDao()
            try{
                beaconDao.setUploaded(mac)
            }catch (e: Exception){
                Log.e("DBHelper", "Error setting uploaded: ${e.message}")
            }
        }
    }
}