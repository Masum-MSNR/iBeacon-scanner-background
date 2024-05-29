package dev.almasum.ibeacon_scanner_background.ibeacon

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, IBeaconScannerService::class.java)
            serviceIntent.action = "START"
            context.startForegroundService(serviceIntent)
        }
    }
}
