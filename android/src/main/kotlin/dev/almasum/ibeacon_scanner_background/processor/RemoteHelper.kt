package dev.almasum.ibeacon_scanner_background.processor

import android.content.Context
import android.util.Log
import dev.almasum.ibeacon_scanner_background.database.local.DatabaseClient
import dev.almasum.ibeacon_scanner_background.database.remote.RequestBody
import dev.almasum.ibeacon_scanner_background.database.remote.WebService
import dev.almasum.ibeacon_scanner_background.ibeacon.IBeaconScannerService.Companion.apiCallRunning
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
            if (beacons.isEmpty()) {
                apiCallRunning = false
                return@launch
            }
            val toUpload = beacons.first()
            val prefs = context.getSharedPreferences("inv_app", Context.MODE_PRIVATE)

            try {
                val requestBody = RequestBody(
                    action = "upload_data",
                    token = prefs.getString("token", "")!!,
                    timestamp = Tools.convertTimestampToString(toUpload.timestamp),
                    latitude = toUpload.latitude.toString(),
                    longitude = toUpload.longitude.toString(),
                    uuid = toUpload.uuid
                )

                Log.d("RemoteHelper", requestBody.toString())
                val response = WebService.create().postBeacon(
                    requestBody
                )

                if (response.asJsonObject.get("result").asString == "ok") {
                    beaconDao.setUploaded(toUpload.mac)
                    Log.d("RemoteHelper", "${toUpload.mac} uploaded")
                }
                updatePeriodic(context)
            } catch (e: Exception) {
                apiCallRunning = false
                Log.e("RemoteHelper", e.message!!)
                e.printStackTrace()
            }
        }
    }
}