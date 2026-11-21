package com.medibox.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.medibox.R
import com.medibox.network.RetrofitClient
import com.medibox.network.SessionManager
import com.medibox.network.model.GenerateCodeDto
import com.medibox.network.model.LinkCodeDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VinculacionActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vinculacion)

        sessionManager = SessionManager(this)

        val perfil = intent.getStringExtra("PERFIL") ?: sessionManager.getUserRole()
        val nombre = intent.getStringExtra("USUARIO_NOMBRE") ?: sessionManager.getUserName()

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

            fun solicitarCodigoApi() {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val authApi = RetrofitClient.getAuthApi(this@VinculacionActivity)
                        val res = authApi.generatePairingCode(
                            GenerateCodeDto(
                                caregiverUserId = sessionManager.getUserId().takeIf { it > 0 },
                                caregiverDeviceId = sessionManager.getDeviceId()
                            )
                        )
                        withContext(Dispatchers.Main) {
                            if (res.isSuccessful && res.body() != null) {
                                val code = res.body()!!.code
                                prefs.edit().putString("codigo_vinculacion", code).apply()
                                tvCodigo.text = code.substring(0, 3) + " " + code.substring(3)
                            } else {
                                // Fallback local
                                val fallback = (100000..999999).random().toString()
                                prefs.edit().putString("codigo_vinculacion", fallback).apply()
                                tvCodigo.text = fallback.substring(0, 3) + " " + fallback.substring(3)
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            val fallback = (100000..999999).random().toString()
                            prefs.edit().putString("codigo_vinculacion", fallback).apply()
                            tvCodigo.text = fallback.substring(0, 3) + " " + fallback.substring(3)
                        }
                    }
                }
            }

            solicitarCodigoApi()
            btnGenerar.setOnClickListener { solicitarCodigoApi() }
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

                btnVincular.isEnabled = false
                btnVincular.text = "Verificando..."

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val authApi = RetrofitClient.getAuthApi(this@VinculacionActivity)
                        val res = authApi.linkWithCode(
                            LinkCodeDto(
                                code = codigo,
                                patientUserId = sessionManager.getUserId().takeIf { it > 0 },
                                patientDeviceId = sessionManager.getDeviceId()
                            )
                        )

                        withContext(Dispatchers.Main) {
                            btnVincular.isEnabled = true
                            btnVincular.text = "Vincular"

                            if (res.isSuccessful && res.body()?.success == true) {
                                val caregiverDev = res.body()?.caregiverDeviceId
                                sessionManager.setLinkedDeviceId(caregiverDev)
                                prefs.edit().putString("codigo_ingresado", codigo).apply()
                                mostrarMensaje("¡Vinculado exitosamente con tu cuidador!", true)
                            } else {
                                mostrarMensaje(res.body()?.message ?: "Código incorrecto o expirado.", false)
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            btnVincular.isEnabled = true
                            btnVincular.text = "Vincular"
                            prefs.edit().putString("codigo_ingresado", codigo).apply()
                            mostrarMensaje("Código guardado localmente (Servidor: ${e.localizedMessage})", true)
                        }
                    }
                }
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
