package dev.almasum.ibeacon_scanner_background.ibeacon

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dev.almasum.ibeacon_scanner_background.IbeaconScannerBackgroundPlugin.Companion.currentStatus
import dev.almasum.ibeacon_scanner_background.processor.LocalHelper
import dev.almasum.ibeacon_scanner_background.processor.RemoteHelper
import dev.almasum.ibeacon_scanner_background.utils.MyNotification
import dev.almasum.ibeacon_scanner_background.utils.Tools
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Arrays


class IBeaconScannerService : Service() {

    private var btScanner: BluetoothLeScanner? = null
    private val eddystoneServiceId: ParcelUuid =
        ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB")
    private var coroutineScope: CoroutineScope? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    companion object {
        var apiCallRunning = false
    }


    private val mLocationRequest: LocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(1000)
            .build()

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (locationResult.locations.isNotEmpty()) {
                for (location in locationResult.locations) {
                    latitude = location.latitude
                    longitude = location.longitude
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun listenLocation() {
        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(applicationContext)
        fusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.getMainLooper()
        )
    }

    private fun startRepeatingFunction(function: () -> Unit) {
        coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope?.launch {
            while (isActive) {
                function()
                delay(60000)
                Log.d("MSNR", "Scanning")
            }
        }
    }

    private fun stopRepeatingFunction() {
        coroutineScope?.cancel()
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {

        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val scanRecord = result.scanRecord
            val beaconModel = BeaconModel(result.device.address)
            beaconModel.manufacturer = result.device.name
            beaconModel.rssi = result.rssi
            if (scanRecord != null) {
                val serviceUuids = scanRecord.serviceUuids

                if (serviceUuids != null && serviceUuids.size > 0 && serviceUuids.contains(
                        eddystoneServiceId
                    )
                ) {
                    val serviceData = scanRecord.getServiceData(eddystoneServiceId)
                    if (serviceData != null && serviceData.size > 18) {
                        val eddystoneUUID =
                            Tools.toHexString(Arrays.copyOfRange(serviceData, 2, 18))
                        val namespace = String(eddystoneUUID.toCharArray().sliceArray(0..19))
                        val instance = String(
                            eddystoneUUID.toCharArray()
                                .sliceArray(20 until eddystoneUUID.toCharArray().size)
                        )
                        beaconModel.type = "ed"
                        beaconModel.namespace = namespace
                        beaconModel.instance = instance
                        beaconModel.uuid = eddystoneUUID

                        var json = "{"
                        json += "\"type\":\"ed\","
                        json += "\"mac\":\"${result.device.address}\","
                        json += "\"rssi\":${result.rssi},"
                        json += "\"uuid\":\"$eddystoneUUID\","
                        json += "\"major\":0,"
                        json += "\"minor\":0,"
                        json += "\"latitude\":$latitude,"
                        json += "\"longitude\":$longitude"
                        json += "}"

                        sendData(json)
                        LocalHelper.checkDbAndInsert(
                            applicationContext,
                            beaconModel,
                            "ed",
                            latitude,
                            longitude
                        )
                    }
                }
                val iBeaconManufactureData = scanRecord.getManufacturerSpecificData(0X004c)
                if (iBeaconManufactureData != null && iBeaconManufactureData.size >= 23) {
                    val iBeaconUUID = Tools.toHexString(iBeaconManufactureData.copyOfRange(2, 18))
                    val major = Integer.parseInt(
                        Tools.toHexString(
                            iBeaconManufactureData.copyOfRange(
                                18,
                                20
                            )
                        ), 16
                    )
                    val minor = Integer.parseInt(
                        Tools.toHexString(
                            iBeaconManufactureData.copyOfRange(
                                20,
                                22
                            )
                        ), 16
                    )
                    beaconModel.type = "ib"
                    beaconModel.uuid = iBeaconUUID
                    beaconModel.major = major
                    beaconModel.minor = minor

                    var json = "{"
                    json += "\"type\":\"ib\","
                    json += "\"mac\":\"${result.device.address}\","
                    json += "\"rssi\":${result.rssi},"
                    json += "\"uuid\":\"$iBeaconUUID\","
                    json += "\"major\":$major,"
                    json += "\"minor\":$minor,"
                    json += "\"latitude\":$latitude,"
                    json += "\"longitude\":$longitude"
                    json += "}"

                    sendData(json)
                    LocalHelper.checkDbAndInsert(
                        applicationContext,
                        beaconModel,
                        "ib",
                        latitude,
                        longitude
                    )
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            currentStatus!!.value = "Inactive"
            Log.e("MSNR", errorCode.toString())
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun sendData(input: String) {
        Log.v("MSNR", "Data: $input")
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
                startForeground(
                    MyNotification.NOTIFICATION_ID,
                    MyNotification.createNotification(
                        this,
                        "iBeacon Scanner",
                        "Scanning for iBeacon and Eddystone beacons",
                        false,
                        isService = true,
                    ),
                )
                val bluetoothAdapter =
                    (applicationContext?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter!!
                btScanner = bluetoothAdapter.bluetoothLeScanner
                startRepeatingFunction {
                    if (!bluetoothAdapter.isEnabled) {
                        MyNotification.showNotification(
                            applicationContext,
                            789,
                            "Bluetooth is off",
                            "Please turn on Bluetooth",
                        )
                        return@startRepeatingFunction
                    }
                    btScanner!!.stopScan(leScanCallback)
                    val filters = mutableListOf<ScanFilter>()
                    filters.add(ScanFilter.Builder().setServiceUuid(eddystoneServiceId).build())
                    filters.add(
                        ScanFilter.Builder().setManufacturerData(0X004c, byteArrayOf()).build()
                    )
                    val scanSetting =
                        ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                            .build()
                    btScanner!!.startScan(filters, scanSetting, leScanCallback)
                    if (currentStatus != null) {
                        currentStatus!!.value = "Scanning"
                    }
                    if (!apiCallRunning) {
                        apiCallRunning = true
                        RemoteHelper.updatePeriodic(applicationContext)
                    }
                }
                listenLocation()
            } else if (intent.action.equals("STOP")) {
                if (currentStatus != null) {
                    currentStatus!!.value = "Inactive"
                }
                btScanner =
                    (applicationContext?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter!!.bluetoothLeScanner
                btScanner!!.stopScan(leScanCallback)
                stopRepeatingFunction()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        btScanner!!.stopScan(leScanCallback)
        super.onDestroy()
    }
}
