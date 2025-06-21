package com.example.eclinic.firebase

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Toast.makeText(context, "Device rebooted, receiver triggered", Toast.LENGTH_SHORT)
                .show()
            // Add your logic after device reboot here
        }
    }
}