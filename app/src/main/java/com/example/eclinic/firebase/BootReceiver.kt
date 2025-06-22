package com.example.eclinic.firebase

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * A [BroadcastReceiver] that listens for the device's boot completion event.
 * When the device has finished booting, it triggers a [Toast] message.
 * This can be used to perform actions (e.g., rescheduling alarms, starting services)
 * after a device reboot.
 */
class BootReceiver : BroadcastReceiver() {
    /**
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast.
     * It specifically checks for [Intent.ACTION_BOOT_COMPLETED] to respond to device reboots.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        // Check if the received intent action indicates that the device has finished booting.
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Display a short Toast message to confirm the receiver was triggered.
            Toast.makeText(context, "Device rebooted, receiver triggered", Toast.LENGTH_SHORT)
                .show()
        }
    }
}