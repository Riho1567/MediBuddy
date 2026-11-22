package com.medibox.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.medibox.R
import com.medibox.network.RetrofitClient
import com.medibox.network.SessionManager
import com.medibox.network.model.LoginDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        val etUsuario = findViewById<TextInputEditText>(R.id.etUsuarioLogin)
        val etPassword = findViewById<TextInputEditText>(R.id.etPasswordLogin)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)

        btnIngresar.setOnClickListener {
            val emailOrUser = etUsuario.text.toString().trim()
            val password = etPassword.text?.toString() ?: ""

            if (emailOrUser.isEmpty()) {
                etUsuario.error = "Ingresa tu correo o usuario"
                return@setOnClickListener
            }

            // Si es un correo, intentamos autenticación REST en la API
            if (emailOrUser.contains("@") && password.isNotEmpty()) {
                btnIngresar.isEnabled = false
                btnIngresar.text = "Iniciando sesión..."

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val authApi = RetrofitClient.getAuthApi(this@LoginActivity)
                        val response = authApi.login(LoginDto(email = emailOrUser, password = password))

                        withContext(Dispatchers.Main) {
                            btnIngresar.isEnabled = true
                            btnIngresar.text = "INGRESAR"

                            if (response.isSuccessful && response.body() != null) {
                                val body = response.body()!!
                                sessionManager.saveSession(
                                    token = body.token,
                                    userId = body.userId,
                                    name = body.name,
                                    email = body.email,
                                    role = body.role,
                                    deviceId = body.deviceId,
                                    linkedDeviceId = body.linkedDeviceId
                                )

                                // Compatibilidad con SharedPreferences existentes
                                getSharedPreferences("medibuddy", Context.MODE_PRIVATE).edit()
                                    .putString("nombre", body.name)
                                    .putString("correo", body.email)
                                    .putString("perfil", body.role)
                                    .apply()

                                startActivity(Intent(this@LoginActivity, MainActivity::class.java).apply {
                                    putExtra("USUARIO_NOMBRE", body.name)
                                    putExtra("PERFIL", body.role)
                                })
                                finish()
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Error al iniciar sesión: Credenciales inválidas.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            btnIngresar.isEnabled = true
                            btnIngresar.text = "INGRESAR"
                            // Si la API no está alcanzable en este momento, permitir acceso en modo offline para pruebas
                            Toast.makeText(
                                this@LoginActivity,
                                "Servidor no disponible (${e.localizedMessage}). Entrando en modo local.",
                                Toast.LENGTH_SHORT
                            ).show()
                            entrarModoLocal(emailOrUser)
                        }
                    }
                }
            } else {
                entrarModoLocal(emailOrUser.ifEmpty { "Paciente" })
            }
        }

        findViewById<Button>(R.id.btnIrRegistro).setOnClickListener {
            startActivity(Intent(this, RegistroCuentaActivity::class.java))
        }
    }

    private fun entrarModoLocal(usuario: String) {
        val prefs = getSharedPreferences("medibuddy", Context.MODE_PRIVATE)
        val perfil = prefs.getString("perfil", "ADULTO_MAYOR")
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra("USUARIO_NOMBRE", usuario)
            putExtra("PERFIL", perfil)
        })
        finish()
    }
}
