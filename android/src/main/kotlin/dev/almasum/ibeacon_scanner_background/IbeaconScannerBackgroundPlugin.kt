package dev.almasum.ibeacon_scanner_background

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import dev.almasum.ibeacon_scanner_background.ibeacon.BeaconModel
import dev.almasum.ibeacon_scanner_background.ibeacon.IBeaconScannerService
import dev.almasum.ibeacon_scanner_background.utils.MyNotification
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


/** IbeaconScannerBackgroundPlugin */
class IbeaconScannerBackgroundPlugin : FlutterPlugin, MethodCallHandler,
    ActivityAware, EventChannel.StreamHandler {

    companion object {
        var currentStatus: MutableLiveData<String>? = null
    }

    private lateinit var channel: MethodChannel
    private lateinit var streamChannle: EventChannel
    private lateinit var context: Context
    private lateinit var activity: Activity
    private var results = mutableListOf<BeaconModel>()
    private var eventSink: EventChannel.EventSink? = null

    private val filters = arrayOf("dev.almasum.ibeacon_scanner_background.DATA","dev.almasum.ibeacon_scanner_background.REFRESH")

    private val intentFilter: IntentFilter by lazy {
        IntentFilter().apply {
            filters.forEach { addAction(it) }
        }
    }

    inner class DataBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "dev.almasum.ibeacon_scanner_background.DATA" -> showData(intent)
                "dev.almasum.ibeacon_scanner_background.REFRESH" -> {
                    Log.d("MSNR","ListRefresh")
                    removeOldResults()
                    eventSink?.success(results.toString())
                }
            }
        }
    }

    fun showData(intent: Intent) {
        val json = intent.getStringExtra("data")!!
        val typeRegex = """"type":"([a-z]{2})"""".toRegex()
        val macRegex = """"mac":"([0-9A-F]{2}(?::[0-9A-F]{2}){5})"""".toRegex()
        val rssiRegex = """"rssi":(-?\d+)""".toRegex()
        val uuidRegex = """"uuid":"([0-9A-F]{32})"""".toRegex()
        val majorRegex = """"major":(\d+)""".toRegex()
        val minorRegex = """"minor":(\d+)""".toRegex()
        val latitudeRegex = """"latitude":(-?\d+\.\d+)""".toRegex()
        val longitudeRegex = """"longitude":(-?\d+\.\d+)""".toRegex()

        val type = typeRegex.find(json)?.groups?.get(1)?.value
        val mac = macRegex.find(json)?.groups?.get(1)?.value
        val rssi = rssiRegex.find(json)?.groups?.get(1)?.value
        val uuid = uuidRegex.find(json)?.groups?.get(1)?.value
        val major = majorRegex.find(json)?.groups?.get(1)?.value
        val minor = minorRegex.find(json)?.groups?.get(1)?.value
        val latitude = latitudeRegex.find(json)?.groups?.get(1)?.value
        val longitude = longitudeRegex.find(json)?.groups?.get(1)?.value

        if (results.contains(BeaconModel(mac))) {
            results[results.indexOf(BeaconModel(mac))].type = type
            results[results.indexOf(BeaconModel(mac))].macAddress = mac
            results[results.indexOf(BeaconModel(mac))].rssi = rssi?.toInt()
            results[results.indexOf(BeaconModel(mac))].uuid = uuid
            results[results.indexOf(BeaconModel(mac))].major = major?.toInt()
            results[results.indexOf(BeaconModel(mac))].minor = minor?.toInt()
            results[results.indexOf(BeaconModel(mac))].latitude = latitude?.toDouble()
            results[results.indexOf(BeaconModel(mac))].longitude = longitude?.toDouble()
            results[results.indexOf(BeaconModel(mac))].timestamp = System.currentTimeMillis()
        } else {
            val beacon = BeaconModel(mac)
            beacon.type = type
            beacon.macAddress = mac
            beacon.rssi = rssi?.toInt()
            beacon.uuid = uuid
            beacon.major = major?.toInt()
            beacon.minor = minor?.toInt()
            beacon.latitude = latitude?.toDouble()
            beacon.longitude = longitude?.toDouble()
            beacon.timestamp = System.currentTimeMillis()
            results.add(beacon)
        }
        removeOldResults()
        eventSink?.success(results.toString())
        Log.d("MSNR", "UpdatedResult: $results")
    }

    private fun removeOldResults() {
        val currentTime = System.currentTimeMillis()
        results.removeAll {
            it.timestamp?.let { timestamp ->
                currentTime - timestamp > 60000
            } ?: false
        }
    }

    private lateinit var broadcastReceiver: IbeaconScannerBackgroundPlugin.DataBroadcastReceiver

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "iBeacon")
        streamChannle = EventChannel(flutterPluginBinding.binaryMessenger, "iBeaconStream")
        streamChannle.setStreamHandler(this)
        context = flutterPluginBinding.applicationContext
        broadcastReceiver = DataBroadcastReceiver()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                broadcastReceiver,
                intentFilter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            context.registerReceiver(broadcastReceiver, intentFilter)
        }
        channel.setMethodCallHandler(this)
        MyNotification.createNotificationChannels(context)
        currentStatus = MutableLiveData()
        currentStatus!!.observeForever {
            if (eventSink != null) {
                eventSink!!.success(it)
            }
        }
        currentStatus!!.value = "Inactive"
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method == "start_scan") {
            val serviceIntent = Intent(context, IBeaconScannerService::class.java)
            serviceIntent.action = "START"
            context.startForegroundService(serviceIntent)
            result.success(true)
        } else if (call.method == "stop_scan") {
            val serviceIntent = Intent(context, IBeaconScannerService::class.java)
            serviceIntent.action = "STOP"
            context.startForegroundService(serviceIntent)
            result.success(true)
        } else if (call.method == "save_token") {
            try {
                val token = call.argument<String>("token")
                val prefEditor =
                    context.getSharedPreferences("inv_app", Context.MODE_PRIVATE).edit()
                prefEditor.putString("token", token)
                prefEditor.apply()
                result.success(true)
            } catch (e: Exception) {
                result.success(false)
            }
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        streamChannle.setStreamHandler(null)
    }

    override fun onDetachedFromActivity() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity;
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onListen(arguments: Any?, eventSink: EventChannel.EventSink?) {
        this.eventSink = eventSink
    }

    override fun onCancel(arguments: Any?) {
        // Handle cancellation of stream
    }
}
