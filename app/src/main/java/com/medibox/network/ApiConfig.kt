package com.medibox.network

import android.content.Context
import com.medibox.BuildConfig

/**
 * Gestor modular de URLs y configuración de la API de MediBuddy.
 * Lee por defecto las variables de entorno inyectadas en tiempo de compilación (BuildConfig),
 * y permite sobreescritura dinámica en tiempo de ejecución (ej. si el usuario especifica una IP de servidor).
 */
object ApiConfig {

    private const val PREFS_NAME = "medibuddy_network_config"
    private const val KEY_CUSTOM_API_URL = "custom_api_url"
    private const val KEY_CUSTOM_WS_URL = "custom_ws_url"

    // URL base por defecto inyectada por variable de entorno Gradle (MEDIBUDDY_API_URL)
    val DEFAULT_API_BASE_URL: String = BuildConfig.API_BASE_URL

    // URL WebSocket por defecto inyectada por variable de entorno Gradle (MEDIBUDDY_WS_URL)
    val DEFAULT_WS_BASE_URL: String = BuildConfig.WS_BASE_URL

    /**
     * Obtiene la URL base de la API HTTP REST.
     * Si el usuario configuró una URL personalizada en tiempo de ejecución, la utiliza;
     * de lo contrario utiliza el valor modular inyectado por el entorno.
     */
    fun getBaseUrl(context: Context? = null): String {
        if (context != null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val custom = prefs.getString(KEY_CUSTOM_API_URL, null)
            if (!custom.isNullOrBlank()) {
                return if (custom.endsWith("/")) custom else "$custom/"
            }
        }
        val url = DEFAULT_API_BASE_URL
        return if (url.endsWith("/")) url else "$url/"
    }

    /**
     * Obtiene la URL del servidor de WebSocket.
     */
    fun getWsUrl(context: Context? = null): String {
        if (context != null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val custom = prefs.getString(KEY_CUSTOM_WS_URL, null)
            if (!custom.isNullOrBlank()) {
                return custom
            }
        }
        return DEFAULT_WS_BASE_URL
    }

    /**
     * Permite modificar modularmente el servidor en tiempo de ejecución.
     */
    fun setCustomServer(context: Context, apiBaseUrl: String?, wsBaseUrl: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            if (apiBaseUrl != null) putString(KEY_CUSTOM_API_URL, apiBaseUrl.trim())
            else remove(KEY_CUSTOM_API_URL)

            if (wsBaseUrl != null) putString(KEY_CUSTOM_WS_URL, wsBaseUrl.trim())
            else remove(KEY_CUSTOM_WS_URL)
            apply()
        }
    }
}
