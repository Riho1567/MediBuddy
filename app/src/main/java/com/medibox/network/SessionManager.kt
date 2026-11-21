package com.medibox.network

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("medibuddy_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_LINKED_DEVICE_ID = "linked_device_id"
    }

    fun saveSession(
        token: String,
        userId: Long,
        name: String,
        email: String,
        role: String,
        deviceId: String,
        linkedDeviceId: String? = null
    ) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putLong(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_ROLE, role)
            putString(KEY_DEVICE_ID, deviceId)
            putString(KEY_LINKED_DEVICE_ID, linkedDeviceId)
            apply()
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "Usuario") ?: "Usuario"

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun getUserRole(): String = prefs.getString(KEY_USER_ROLE, "ADULTO_MAYOR") ?: "ADULTO_MAYOR"

    fun isCuidador(): Boolean = getUserRole() == "CUIDADOR"

    fun getDeviceId(): String {
        var devId = prefs.getString(KEY_DEVICE_ID, null)
        if (devId.isNullOrBlank()) {
            devId = "ANDROID_USER_" + System.currentTimeMillis()
            prefs.edit().putString(KEY_DEVICE_ID, devId).apply()
        }
        return devId
    }

    fun getLinkedDeviceId(): String? = prefs.getString(KEY_LINKED_DEVICE_ID, null)

    fun setLinkedDeviceId(id: String?) {
        prefs.edit().putString(KEY_LINKED_DEVICE_ID, id).apply()
    }

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
