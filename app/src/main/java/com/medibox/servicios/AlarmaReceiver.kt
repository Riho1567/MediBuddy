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
import com.medibox.ui.AvisoDosisActivity

class AlarmaReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val p = context.getSharedPreferences("medibuddy", Context.MODE_PRIVATE)
        val nombre = p.getString("med_nombre", "Medicamento") ?: "Medicamento"
        val dosis = p.getString("med_dosis", "1 dosis") ?: "1 dosis"
        val h = (p.all["med_hora"] as? Number)?.toInt() ?: 0
        val m = (p.all["med_minuto"] as? Number)?.toInt() ?: 0
        val hora = String.format("%d:%02d", h, m)

        mostrarNotificacion(context, nombre, dosis, hora)

        // Deja programado el aviso de mañana.
        AlarmScheduler.programarDesdePrefs(context)
    }

    private fun mostrarNotificacion(context: Context, medicamento: String, dosis: String, hora: String) {
        val canalId = "canal_tomas_medibox"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                canalId, "Avisos de Tomas MediBox", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para la toma de medicamentos"
                enableVibration(true)
            }
            manager.createNotificationChannel(canal)
        }

        val intentAviso = Intent(context, AvisoDosisActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("MEDICAMENTO", medicamento)
            putExtra("DOSIS", dosis)
            putExtra("HORA", hora)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intentAviso,
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
            .setFullScreenIntent(pendingIntent, true)
            .build()

        manager.notify(1001, notificacion)
    }
}