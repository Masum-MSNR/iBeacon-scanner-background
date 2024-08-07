package dev.almasum.ibeacon_scanner_background.processor

import android.content.Context
import android.util.Log
import dev.almasum.ibeacon_scanner_background.database.remote.RequestBody
import dev.almasum.ibeacon_scanner_background.database.remote.WebService
import dev.almasum.ibeacon_scanner_background.ibeacon.IBeaconScannerService.Companion.apiCallRunning
import dev.almasum.ibeacon_scanner_background.utils.Tools
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object RemoteHelper {

    fun updatePeriodic(context: Context) {
        if(apiCallRunning) return
        apiCallRunning = true
        CoroutineScope(Dispatchers.IO).launch {
            val beacons = LocalHelper.getAllCachedBeacon(context)
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
                Log.d("RemoteHelper", response.toString())
                Log.d("RemoteHelper", response.asJsonObject.get("result").toString())
                if (response.asJsonObject.get("result").asString == "ok") {
                    LocalHelper.updateBeacon(context, toUpload.id)
                    Log.d("RemoteHelper", "${toUpload.mac} -> ${toUpload.id} uploaded")
                }
                apiCallRunning = false
            } catch (e: Exception) {
                apiCallRunning = false
                Log.e("RemoteHelper", e.message!!)
                e.printStackTrace()
            }
        }
    }
}