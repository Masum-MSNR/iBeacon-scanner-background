package dev.almasum.ibeacon_scanner_background

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
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
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var streamChannle: EventChannel
    private lateinit var context: Context
    private lateinit var activity: Activity
    private var results = mutableListOf<BeaconModel>()
    private var eventSink: EventChannel.EventSink? = null

    private val filters = arrayOf("dev.almasum.ibeacon_scanner_background.DATA")

    private val intentFilter: IntentFilter by lazy {
        IntentFilter().apply {
            filters.forEach { addAction(it) }
        }
    }

    inner class DataBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "dev.almasum.ibeacon_scanner_background.DATA" -> showData(intent)
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
        var rssi = rssiRegex.find(json)?.groups?.get(1)?.value
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
        } else {
            val beacon = BeaconModel(mac)
            beacon.rssi = rssi?.toInt()
            beacon.uuid = uuid
            beacon.major = major?.toInt()
            beacon.minor = minor?.toInt()
            results.add(beacon)
        }
        eventSink?.success(results.toString())
        Log.d("MSNR", "UpdatedResult: $results")
    }

    private lateinit var broadcastReceiver: IbeaconScannerBackgroundPlugin.DataBroadcastReceiver

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "iBeacon")
        streamChannle = EventChannel(flutterPluginBinding.binaryMessenger, "iBeaconStream")
        streamChannle.setStreamHandler(this)
        context = flutterPluginBinding.applicationContext
        broadcastReceiver = DataBroadcastReceiver()
        context.registerReceiver(broadcastReceiver, intentFilter)
        channel.setMethodCallHandler(this)
        MyNotification.createNotificationChannels(context)
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
        } else {
            Log.d("MSNR", "Method not implemented")
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
