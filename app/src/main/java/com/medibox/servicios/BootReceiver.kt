package com.medibox.servicios

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Al reiniciar el celular las alarmas se pierden: aquí se vuelve a programar. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            AlarmScheduler.programarDesdePrefs(context)
        }
    }
}