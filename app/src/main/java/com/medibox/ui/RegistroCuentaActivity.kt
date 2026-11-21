package com.medibox.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.medibox.R

class RegistroCuentaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_cuenta)

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

            val perfil = if (rgPerfil.checkedRadioButtonId == R.id.rbCuidador)
                "CUIDADOR" else "ADULTO_MAYOR"

            // Guardado local temporal; luego se reemplaza por la llamada al servidor.
            getSharedPreferences("medibuddy", Context.MODE_PRIVATE).edit()
                .putString("nombre", nombre)
                .putString("correo", correo)
                .putString("perfil", perfil)
                .apply()

            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("USUARIO_NOMBRE", nombre)
                putExtra("PERFIL", perfil)
            })
            finishAffinity()
        }
    }
}