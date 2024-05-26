package dev.almasum.ibeacon_scanner_background

import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import android.content.Intent
import android.app.Activity
import android.content.Context
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.util.Log
import android.os.Bundle

/** IbeaconScannerBackgroundPlugin */
class IbeaconScannerBackgroundPlugin :  FlutterPlugin, MethodCallHandler,
    ActivityAware, EventChannel.StreamHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var eventChannel : EventChannel
    private lateinit var context: Context
    private lateinit var activity: Activity
    private var results = mutableListOf<Beacon>()
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
        val macRegex = """"mac":"([0-9A-F]{2}(?::[0-9A-F]{2}){5})"""".toRegex()
        val rssiRegex = """"rssi":(-?\d+)""".toRegex()
        val uuidRegex = """"uuid":"([0-9A-F]{32})"""".toRegex()
        val majorRegex = """"major":(\d+)""".toRegex()
        val minorRegex = """"minor":(\d+)""".toRegex()

        val mac = macRegex.find(json)?.groups?.get(1)?.value
        var rssi = rssiRegex.find(json)?.groups?.get(1)?.value
        val uuid = uuidRegex.find(json)?.groups?.get(1)?.value
        val major = majorRegex.find(json)?.groups?.get(1)?.value
        val minor = minorRegex.find(json)?.groups?.get(1)?.value
        if (results.contains(Beacon(mac))) {
            results[results.indexOf(Beacon(mac))].macAddress = mac
            results[results.indexOf(Beacon(mac))].rssi = rssi?.toInt()
            results[results.indexOf(Beacon(mac))].uuid = uuid
            results[results.indexOf(Beacon(mac))].major = major?.toInt()
            results[results.indexOf(Beacon(mac))].minor = minor?.toInt()
        } else {
            val beacon = Beacon(mac)
            beacon.rssi = rssi?.toInt()
            beacon.uuid = uuid
            beacon.major = major?.toInt()
            beacon.minor = minor?.toInt()
            results.add(beacon)
        }
        eventSink?.success(results.toString())
        println("UpdatedResult: $results")
    }

    private lateinit var broadcastReceiver: IbeaconScannerBackgroundPlugin.DataBroadcastReceiver

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "iBeacon")
        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "iBeaconStream")
        eventChannel.setStreamHandler(this)
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        broadcastReceiver = DataBroadcastReceiver()
        context.registerReceiver(broadcastReceiver, intentFilter)
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
        }else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        eventChannel.setStreamHandler(null)

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
        this.eventSink=eventSink
    }

    override fun onCancel(arguments: Any?) {
        // Handle cancellation of stream
    }
}
