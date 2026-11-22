package com.medibox.servicios

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.medibox.R
import com.medibox.ui.AvisoDosisActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmaReceiver : BroadcastReceiver() {

    companion object {
        private const val CANAL_AVISO = "medibuddy_recordatorios"
        private const val CANAL_TOMADA = "medibuddy_confirmaciones"

        // Notificación "Medicamento tomado", se llama al confirmar la toma.
        fun mostrarTomada(context: Context, medicamento: String) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                manager.getNotificationChannel(CANAL_TOMADA) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CANAL_TOMADA, "Confirmaciones de toma",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply { description = "Avisa cuando se confirma que el medicamento fue tomado" }
                )
            }
            val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val texto = "$medicamento fue tomado a las $hora"
            val n = NotificationCompat.Builder(context, CANAL_TOMADA)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("MediBuddy · Medicamento tomado ✓")
                .setContentText(texto)
                .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            try { manager.notify(1002, n) } catch (e: SecurityException) { }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val p = context.getSharedPreferences("medibuddy", Context.MODE_PRIVATE)
        val nombre = p.getString("med_nombre", "Medicamento") ?: "Medicamento"
        val dosis = p.getString("med_dosis", "1 dosis") ?: "1 dosis"
        val h = (p.all["med_hora"] as? Number)?.toInt() ?: 0
        val m = (p.all["med_minuto"] as? Number)?.toInt() ?: 0
        val hora = String.format("%d:%02d", h, m)

        mostrarAviso(context, nombre, dosis, hora)
        AlarmScheduler.programarDesdePrefs(context)   // deja lista la de mañana
    }

    private fun mostrarAviso(context: Context, medicamento: String, dosis: String, hora: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val sonido = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            manager.getNotificationChannel(CANAL_AVISO) == null) {
            val canal = NotificationChannel(
                CANAL_AVISO, "Recordatorios de medicamentos", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos cuando es hora de tomar un medicamento"
                enableVibration(true)
                setSound(
                    sonido,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            manager.createNotificationChannel(canal)
        }

        val intentAviso = Intent(context, AvisoDosisActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("MEDICAMENTO", medicamento)
            putExtra("DOSIS", dosis)
            putExtra("HORA", hora)
        }
        val pending = PendingIntent.getActivity(
            context, 0, intentAviso,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val texto = "Es hora de tomar $medicamento ($dosis)"
        val n = NotificationCompat.Builder(context, CANAL_AVISO)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("MediBuddy")
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setFullScreenIntent(pending, true)
            .setSound(sonido)
            .setVibrate(longArrayOf(0, 500, 300, 500, 300, 500))
            .build()

        try { manager.notify(1001, n) } catch (e: SecurityException) { }
    }
}