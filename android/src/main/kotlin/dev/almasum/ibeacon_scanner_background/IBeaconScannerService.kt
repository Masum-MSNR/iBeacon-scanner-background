package dev.almasum.ibeacon_scanner_background

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.Arrays

class IBeaconScannerService : Service() {
    private var btScanner: BluetoothLeScanner? = null
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val scanRecord = result.scanRecord
            val beacon = Beacon(result.device.address)
            beacon.manufacturer = result.device.name
            beacon.rssi = result.rssi
            if (scanRecord != null) {
                val iBeaconManufactureData = scanRecord.getManufacturerSpecificData(0X004c)
                if (iBeaconManufactureData != null && iBeaconManufactureData.size >= 23) {
                    val iBeaconUUID = Utils.toHexString(iBeaconManufactureData.copyOfRange(2, 18))
                    val major = Integer.parseInt(
                        Utils.toHexString(
                            iBeaconManufactureData.copyOfRange(
                                18,
                                20
                            )
                        ), 16
                    )
                    val minor = Integer.parseInt(
                        Utils.toHexString(
                            iBeaconManufactureData.copyOfRange(
                                20,
                                22
                            )
                        ), 16
                    )
                    beacon.type = Beacon.beaconType.iBeacon
                    beacon.uuid = iBeaconUUID
                    beacon.major = major
                    beacon.minor = minor

                    var json = "{"
                    json += "\"mac\":\"${result.device.address}\","
                    json += "\"rssi\":${result.rssi},"
                    json += "\"uuid\":\"$iBeaconUUID\","
                    json += "\"major\":$major,"
                    json += "\"minor\":$minor"
                    json += "}"

                    sendData(json)
                    Log.v("MSNR", "iBeaconUUID:$iBeaconUUID major:$major minor:$minor")
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("MSNR", errorCode.toString())
        }
    }
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "ForegroundServiceChannel"
        private const val NOTIFICATION_ID = 101
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun sendData(input: String) {
        Intent().run {
            action = "dev.almasum.ibeacon_scanner_background.DATA"
            putExtra("data", input)
            applicationContext.sendBroadcast(this)
        }
    }

    @SuppressLint("MissingPermission", "ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (intent.action.equals("START")) {
                startForeground(NOTIFICATION_ID, createNotification())
                btScanner =
                    (applicationContext?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter!!.bluetoothLeScanner
                btScanner!!.startScan(leScanCallback)
                // your start service code
            } else if (intent.action.equals("STOP")) {
                btScanner =
                    (applicationContext?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter!!.bluetoothLeScanner
                btScanner!!.stopScan(leScanCallback)
                stopForeground(true)
                stopSelf()
                stopSelfResult(startId);
            }
        }
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Scanner")
            .setContentText("Scanning for iBeacons...")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .build()
    }


}
