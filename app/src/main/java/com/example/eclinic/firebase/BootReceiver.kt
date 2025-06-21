package com.example.eclinic.firebase

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * A [BroadcastReceiver] that listens for the [Intent.ACTION_BOOT_COMPLETED] broadcast.
 * This receiver is triggered when the device has finished booting up.
 *
 * Currently, it simply displays a [Toast] message to indicate that it has been triggered.
 * Additional logic can be added within the [onReceive] method to perform actions
 * when the device reboots (e.g., re-scheduling alarms, starting services).
 */
class BootReceiver : BroadcastReceiver() {
    /**
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        // Check if the received intent's action is ACTION_BOOT_COMPLETED,
        // which signifies that the device has finished booting.
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Display a short Toast message to the user indicating the receiver was triggered.
            Toast.makeText(context, "Device rebooted, receiver triggered", Toast.LENGTH_SHORT)
                .show()
            // TODO: Add your custom logic here that needs to be executed after the device reboots.
            // For example, you might want to restart a background service, re-schedule notifications, etc.
        }
    }
}