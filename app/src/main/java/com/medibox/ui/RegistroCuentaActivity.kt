package com.medibox.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.medibox.R
import com.medibox.network.RetrofitClient
import com.medibox.network.SessionManager
import com.medibox.network.model.RegisterDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistroCuentaActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_cuenta)

        sessionManager = SessionManager(this)

        val etNombre = findViewById<TextInputEditText>(R.id.etNombreCuenta)
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreoCuenta)
        val etPassword = findViewById<TextInputEditText>(R.id.etPasswordCuenta)
        val rgPerfil = findViewById<RadioGroup>(R.id.rgPerfil)
        val tvError = findViewById<TextView>(R.id.tvErrorCuenta)
        val btnCrear = findViewById<MaterialButton>(R.id.btnCrearCuenta)
        val btnVolver = findViewById<MaterialButton>(R.id.btnVolverLogin)

        btnVolver.setOnClickListener { finish() }

        btnCrear.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val password = etPassword.text.toString()

            val error = when {
                nombre.isEmpty() -> "Escribe tu nombre"
                !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches() ->
                    "Escribe un correo válido"
                password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
                rgPerfil.checkedRadioButtonId == -1 -> "Elige tu perfil"
                else -> null
            }

            if (error != null) {
                tvError.text = error
                tvError.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            tvError.visibility = TextView.GONE
            val perfil = if (rgPerfil.checkedRadioButtonId == R.id.rbCuidador)
                "CUIDADOR" else "ADULTO_MAYOR"

            btnCrear.isEnabled = false
            btnCrear.text = "Creando cuenta..."

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val authApi = RetrofitClient.getAuthApi(this@RegistroCuentaActivity)
                    val response = authApi.register(
                        RegisterDto(
                            name = nombre,
                            email = correo,
                            password = password,
                            role = perfil
                        )
                    )

                    withContext(Dispatchers.Main) {
                        btnCrear.isEnabled = true
                        btnCrear.text = "Crear cuenta"

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
                        } else {
                            Toast.makeText(
                                this@RegistroCuentaActivity,
                                "Aviso: No se pudo registrar en la nube. Guardando localmente.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        // Guardado local
                        getSharedPreferences("medibuddy", Context.MODE_PRIVATE).edit()
                            .putString("nombre", nombre)
                            .putString("correo", correo)
                            .putString("perfil", perfil)
                            .apply()

                        startActivity(Intent(this@RegistroCuentaActivity, VinculacionActivity::class.java).apply {
                            putExtra("USUARIO_NOMBRE", nombre)
                            putExtra("PERFIL", perfil)
                        })
                        finishAffinity()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        btnCrear.isEnabled = true
                        btnCrear.text = "Crear cuenta"

                        Toast.makeText(
                            this@RegistroCuentaActivity,
                            "Modo offline activo: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()

                        getSharedPreferences("medibuddy", Context.MODE_PRIVATE).edit()
                            .putString("nombre", nombre)
                            .putString("correo", correo)
                            .putString("perfil", perfil)
                            .apply()

                        startActivity(Intent(this@RegistroCuentaActivity, VinculacionActivity::class.java).apply {
                            putExtra("USUARIO_NOMBRE", nombre)
                            putExtra("PERFIL", perfil)
                        })
                        finishAffinity()
                    }
                }
            }
        }
    }
}
