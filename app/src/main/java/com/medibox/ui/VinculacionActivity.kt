package com.medibox.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.medibox.R

class VinculacionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vinculacion)

        val perfil = intent.getStringExtra("PERFIL") ?: "ADULTO_MAYOR"
        val nombre = intent.getStringExtra("USUARIO_NOMBRE") ?: "Paciente"

        val tvTitulo = findViewById<TextView>(R.id.tvTituloVinc)
        val tvDesc = findViewById<TextView>(R.id.tvDescVinc)
        val bloqueCuidador = findViewById<View>(R.id.bloqueCuidador)
        val bloqueAdulto = findViewById<View>(R.id.bloqueAdulto)
        val tvCodigo = findViewById<TextView>(R.id.tvCodigo)
        val btnGenerar = findViewById<MaterialButton>(R.id.btnGenerarCodigo)
        val etCodigo = findViewById<TextInputEditText>(R.id.etCodigoVinc)
        val btnVincular = findViewById<MaterialButton>(R.id.btnVincular)
        val tvMensaje = findViewById<TextView>(R.id.tvMensajeVinc)
        val btnContinuar = findViewById<MaterialButton>(R.id.btnContinuarVinc)

        val prefs = getSharedPreferences("medibuddy", Context.MODE_PRIVATE)

        fun mostrarMensaje(texto: String, ok: Boolean) {
            tvMensaje.text = texto
            tvMensaje.setTextColor(Color.parseColor(if (ok) "#0B5D2A" else "#B71C1C"))
            tvMensaje.visibility = View.VISIBLE
        }

        if (perfil == "CUIDADOR") {
            tvTitulo.text = "Vincula al adulto mayor"
            tvDesc.text = "Dale este código a la persona que cuidas. Debe escribirlo en su celular."
            bloqueCuidador.visibility = View.VISIBLE

            fun generarCodigo() {
                val codigo = (100000..999999).random().toString()
                prefs.edit()
                    .putString("codigo_vinculacion", codigo)
                    .putLong("codigo_expira", System.currentTimeMillis() + 10 * 60 * 1000)
                    .apply()
                tvCodigo.text = codigo.substring(0, 3) + " " + codigo.substring(3)
            }
            generarCodigo()
            btnGenerar.setOnClickListener { generarCodigo() }
        } else {
            tvTitulo.text = "Vincúlate con tu cuidador"
            tvDesc.text = "Escribe el código de 6 dígitos que te dio tu cuidador."
            bloqueAdulto.visibility = View.VISIBLE

            btnVincular.setOnClickListener {
                val codigo = etCodigo.text.toString().trim()
                if (codigo.length != 6) {
                    mostrarMensaje("El código debe tener 6 dígitos", false)
                    return@setOnClickListener
                }
                // TEMPORAL: aquí se llamará al servidor para validar el código.
                prefs.edit().putString("codigo_ingresado", codigo).apply()
                mostrarMensaje("Código enviado. Falta conectar con el servidor.", true)
            }
        }

        btnContinuar.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("USUARIO_NOMBRE", nombre)
                putExtra("PERFIL", perfil)
            })
            finishAffinity()
        }
    }
}