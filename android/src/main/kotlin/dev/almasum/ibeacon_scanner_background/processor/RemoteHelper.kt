package dev.almasum.ibeacon_scanner_background.processor

import android.content.Context
import android.util.Log
import dev.almasum.ibeacon_scanner_background.database.local.DatabaseClient
import dev.almasum.ibeacon_scanner_background.database.remote.RequestBody
import dev.almasum.ibeacon_scanner_background.database.remote.WebService
import dev.almasum.ibeacon_scanner_background.utils.Tools
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object RemoteHelper {
    fun updatePeriodic(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = DatabaseClient.getDatabase(context)
            val beaconDao = db.beaconDao()
            val beacons = beaconDao.getAllCachedBeacon()
            if (beacons.isEmpty()) return@launch
            val toUpload = beacons.first()
            val prefs = context.getSharedPreferences("inv_app", Context.MODE_PRIVATE)

            try {
                val response = WebService.create().postBeacon(
                    RequestBody(
                        action = "upload_data",
                        token = prefs.getString("token", "")!!,
                        timestamp = Tools.convertTimestampToString(toUpload.timestamp),
                        latitude = toUpload.latitude.toString(),
                        longitude = toUpload.longitude.toString(),
                        uuid = toUpload.uuid
                    )
                )

                if (response.asJsonObject["result"].toString() == "ok") {
                    Log.d("RemoteHelper", toUpload.toString())
                    beaconDao.setUploaded(toUpload.mac)
                }
                updatePeriodic(context)
            } catch (e: Exception) {
                Log.e("RemoteHelper", e.message!!)
            }
        }
    }
}