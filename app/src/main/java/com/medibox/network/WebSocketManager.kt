package com.medibox.network

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.medibox.network.model.DeviceMessageDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketManager(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var webSocket: WebSocket? = null
    private var isConnected = false

    private val _eventsFlow = MutableSharedFlow<DeviceMessageDto>(extraBufferCapacity = 64)
    val eventsFlow: SharedFlow<DeviceMessageDto> = _eventsFlow.asSharedFlow()

    private val _connectionStateFlow = MutableSharedFlow<Boolean>(replay = 1)
    val connectionStateFlow: SharedFlow<Boolean> = _connectionStateFlow.asSharedFlow()

    companion object {
        private const val TAG = "MediBuddyWS"

        @Volatile
        private var INSTANCE: WebSocketManager? = null

        fun getInstance(context: Context): WebSocketManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WebSocketManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun connect() {
        if (isConnected) return

        val sessionManager = SessionManager(context)
        val deviceId = sessionManager.getDeviceId()
        val token = sessionManager.getToken() ?: "meddibuddy-jwt-secret-android-token-2026"
        val wsBaseUrl = ApiConfig.getWsUrl(context)

        val fullUrl = "$wsBaseUrl?deviceId=$deviceId&clientType=ANDROID&token=$token"
        Log.d(TAG, "Conectando WebSocket a: $fullUrl")

        val request = Request.Builder().url(fullUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.i(TAG, "WebSocket conectado con éxito.")
                isConnected = true
                scope.launch { _connectionStateFlow.emit(true) }

                // Si hay un dispositivo vinculado (ESP32 o cuidador), nos suscribimos
                val linkedId = sessionManager.getLinkedDeviceId() ?: "ESP32_MED_01"
                val subscribeMsg = DeviceMessageDto(
                    type = "SUBSCRIBE",
                    deviceId = deviceId,
                    clientType = "ANDROID",
                    targetDeviceId = linkedId
                )
                ws.send(gson.toJson(subscribeMsg))
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d(TAG, "Mensaje WebSocket recibido: $text")
                try {
                    val message = gson.fromJson(text, DeviceMessageDto::class.java)
                    scope.launch {
                        _eventsFlow.emit(message)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error deserializando mensaje: ${e.message}")
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Falla en WebSocket: ${t.message}")
                isConnected = false
                scope.launch { _connectionStateFlow.emit(false) }
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.w(TAG, "WebSocket cerrado: $reason (código $code)")
                isConnected = false
                scope.launch { _connectionStateFlow.emit(false) }
            }
        })
    }

    fun sendCommand(targetDeviceId: String, action: String, params: Map<String, Any?> = emptyMap()): Boolean {
        if (!isConnected || webSocket == null) {
            Log.w(TAG, "No se puede enviar comando: WebSocket no conectado")
            return false
        }
        val sessionManager = SessionManager(context)
        val command = DeviceMessageDto(
            type = "COMMAND",
            deviceId = sessionManager.getDeviceId(),
            clientType = "ANDROID",
            targetDeviceId = targetDeviceId,
            payload = mapOf("action" to action) + params
        )
        return webSocket?.send(gson.toJson(command)) ?: false
    }

    fun disconnect() {
        webSocket?.close(1000, "Cierre normal de la aplicación")
        webSocket = null
        isConnected = false
    }
}
