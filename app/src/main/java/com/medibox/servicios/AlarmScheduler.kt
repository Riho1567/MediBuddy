package com.medibox.servicios

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object AlarmScheduler {

    private const val REQ = 2001

    // Lee el medicamento guardado y programa la alarma de la próxima hora (hoy o mañana).
    fun programarDesdePrefs(context: Context) {
        val p = context.getSharedPreferences("medibuddy", Context.MODE_PRIVATE)
        val nombre = p.getString("med_nombre", null)
        val hora = (p.all["med_hora"] as? Number)?.toInt()
        val minuto = (p.all["med_minuto"] as? Number)?.toInt()
        if (nombre == null || hora == null || minuto == null) {
            cancelar(context)
            return
        }
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        programar(context, cal.timeInMillis)
    }

    // Para "Recordármelo en 5 min".
    fun programarEnMinutos(context: Context, minutos: Int) {
        programar(context, System.currentTimeMillis() + minutos * 60_000L)
    }

    fun cancelar(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context))
    }

    private fun programar(context: Context, momento: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context)
        val puedeExacta = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
        if (puedeExacta) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, momento, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, momento, pi)
        }
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlarmaReceiver::class.java)
        return PendingIntent.getBroadcast(
            context, REQ, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
