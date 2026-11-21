package com.medibox.network.model

import com.google.gson.annotations.SerializedName

// --- Autenticación ---

data class RegisterDto(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String,
    @SerializedName("customDeviceId") val customDeviceId: String? = null
)

data class LoginDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class AuthResponseDto(
    @SerializedName("token") val token: String,
    @SerializedName("userId") val userId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("linkedDeviceId") val linkedDeviceId: String? = null
)

// --- Vinculación Cuidador / Adulto Mayor ---

data class GenerateCodeDto(
    @SerializedName("caregiverUserId") val caregiverUserId: Long? = null,
    @SerializedName("caregiverDeviceId") val caregiverDeviceId: String
)

data class PairingCodeResponseDto(
    @SerializedName("code") val code: String,
    @SerializedName("expiresAt") val expiresAt: String,
    @SerializedName("caregiverDeviceId") val caregiverDeviceId: String
)

data class LinkCodeDto(
    @SerializedName("code") val code: String,
    @SerializedName("patientUserId") val patientUserId: Long? = null,
    @SerializedName("patientDeviceId") val patientDeviceId: String
)

data class LinkResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("caregiverDeviceId") val caregiverDeviceId: String?,
    @SerializedName("patientDeviceId") val patientDeviceId: String
)

// --- Medicamentos y Horarios ---

data class CreateScheduleDto(
    @SerializedName("medicationName") val medicationName: String,
    @SerializedName("dose") val dose: String,
    @SerializedName("compartment") val compartment: Int = 1,
    @SerializedName("scheduledHour") val scheduledHour: Int,
    @SerializedName("scheduledMinute") val scheduledMinute: Int
)

data class UpdateScheduleDto(
    @SerializedName("medicationName") val medicationName: String,
    @SerializedName("dose") val dose: String,
    @SerializedName("scheduledHour") val scheduledHour: Int,
    @SerializedName("scheduledMinute") val scheduledMinute: Int,
    @SerializedName("clientType") val clientType: String = "CUIDADOR"
)

data class UpdateStatusDto(
    @SerializedName("estadoToma") val estadoToma: String
)

data class ScheduleDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("medicationName") val medicationName: String,
    @SerializedName("dose") val dose: String,
    @SerializedName("compartment") val compartment: Int,
    @SerializedName("scheduledHour") val scheduledHour: Int,
    @SerializedName("scheduledMinute") val scheduledMinute: Int,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("estadoToma") val estadoToma: String
)

// --- Comandos Directos (hacia ESP32) ---

data class CommandDto(
    @SerializedName("action") val action: String,
    @SerializedName("params") val params: Map<String, Any?> = emptyMap()
)

data class CommandResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("deviceId") val deviceId: String? = null,
    @SerializedName("action") val action: String? = null,
    @SerializedName("message") val message: String? = null
)

// --- Mensajes WebSocket Bidireccionales ---

data class DeviceMessageDto(
    @SerializedName("type") val type: String,
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("clientType") val clientType: String = "ANDROID",
    @SerializedName("targetDeviceId") val targetDeviceId: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("payload") val payload: Map<String, Any?> = emptyMap(),
    @SerializedName("messageId") val messageId: String? = null
)
