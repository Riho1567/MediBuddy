package com.medibox.conexion

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import java.net.HttpURLConnection
import java.net.URL

/**
 * Habla con el ESP32 por WiFi usando HTTP simple.
 *
 * Rutas del firmware (medibox_esp32.ino):
 *   GET /status    -> "OK"
 *   GET /dispense  -> mueve el servo y enciende el LED rojo ("Dispensando", o 409 si está ocupado)
 *   GET /taken     -> apaga el rojo y enciende el azul ("Tomado")
 *   GET /estado    -> "reposo", "pendiente" (esperando el botón) o "tomado" (botón presionado)
 *   GET /reset     -> deja todo en reposo
 *
 * Las peticiones se fuerzan por la red WiFi, así funcionan aunque el celular tenga
 * datos móviles y la red del ESP32 no tenga internet.
 */
class Esp32Client(context: Context) {

    companion object {
        // true = simula el pastillero. Ponlo en false para probar con el ESP32 real.
        const val MODO_DEMO = false
        const val IP = "192.168.4.1"
    }

    private val appContext = context.applicationContext
    private val main = Handler(Looper.getMainLooper())

    fun status(callback: (Boolean, String) -> Unit) = request("/status", callback)
    fun dispense(callback: (Boolean, String) -> Unit) = request("/dispense", callback)
    fun taken(callback: (Boolean, String) -> Unit) = request("/taken", callback)
    git add .    fun estado(callback: (Boolean, String) -> Unit) = request("/estado", callback)
    fun reset(callback: (Boolean, String) -> Unit) = request("/reset", callback)

    @Suppress("DEPRECATION")
    private fun wifiNetwork(): Network? {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        for (n in cm.allNetworks) {
            val caps = cm.getNetworkCapabilities(n) ?: continue
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return n
        }
        return null
    }

    /** callback(éxito, mensaje) siempre se ejecuta en el hilo principal. */
    private fun request(path: String, callback: (Boolean, String) -> Unit) {
        if (MODO_DEMO) {
            val respuesta = if (path == "/estado") "pendiente" else "Modo demo: dispositivo simulado"
            main.postDelayed({ callback(true, respuesta) }, 700)
            return
        }
        Thread {
            var ok = false
            var msg: String
            var conn: HttpURLConnection? = null
            try {
                val url = URL("http://$IP$path")
                val wifi = wifiNetwork()
                conn = (if (wifi != null) wifi.openConnection(url) else url.openConnection())
                        as HttpURLConnection
                conn.connectTimeout = 4000
                conn.readTimeout = 6000
                conn.requestMethod = "GET"
                val code = conn.responseCode
                val body = try {
                    conn.inputStream.bufferedReader().readText().trim()
                } catch (e: Exception) {
                    ""
                }
                ok = code in 200..299
                msg = if (ok) (if (body.isEmpty()) "OK" else body) else "Error HTTP $code"
            } catch (e: Exception) {
                msg = "No se pudo conectar con el dispensador (${e.javaClass.simpleName})"
            } finally {
                conn?.disconnect()
            }
            val finalOk = ok
            val finalMsg = msg
            main.post { callback(finalOk, finalMsg) }
        }.start()
    }
}
