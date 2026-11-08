package com.medibox.servicios

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.medibox.R
import com.medibox.ui.MainActivity

class AlarmaReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicamento = intent.getStringExtra("MEDICAMENTO_NOMBRE") ?: "Medicamento"
        val dosis = intent.getStringExtra("MEDICAMENTO_DOSIS") ?: "1 dosis"

        mostrarNotificacion(context, medicamento, dosis)
    }

    private fun mostrarNotificacion(context: Context, medicamento: String, dosis: String) {
        val canalId = "canal_tomas_medibox"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                canalId,
                "Avisos de Tomas MediBox",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones urgentes para la toma de medicamentos"
                enableVibration(true)
            }
            manager.createNotificationChannel(canal)
        }

        // Intención para abrir la app al tocar la notificación
        val intentApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intentApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, canalId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("¡Hora de tu medicamento!")
            .setContentText("Es momento de tomar: $medicamento ($dosis)")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(1001, notificacion)
    }
}