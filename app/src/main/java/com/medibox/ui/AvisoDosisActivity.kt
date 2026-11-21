package com.medibox.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.medibox.R

class AvisoDosisActivity : AppCompatActivity() {

    // Estados del aviso. Por ahora solo estos dos; los demás se agregan aquí.
    enum class Estado { PENDIENTE, PENDIENTE_SIN_CONEXION }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aviso_dosis)

        // Pantalla completa sobre el bloqueo y pantalla encendida
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        val medicamento = intent.getStringExtra("MEDICAMENTO") ?: "Naproxeno"
        val dosis = intent.getStringExtra("DOSIS") ?: "1 tableta · 500 mg"
        val hora = intent.getStringExtra("HORA") ?: "8:00 p. m."
        val conectado = intent.getBooleanExtra("PASTILLERO_CONECTADO", true)

        findViewById<TextView>(R.id.tvMedicamento).text = medicamento
        findViewById<TextView>(R.id.tvDosis).text = dosis
        findViewById<TextView>(R.id.tvHora).text = "Hora programada: $hora"

        aplicarEstado(if (conectado) Estado.PENDIENTE else Estado.PENDIENTE_SIN_CONEXION)

        findViewById<MaterialButton>(R.id.btnDispensar).setOnClickListener {
            // TEMPORAL: aquí se enviará la orden HTTP al ESP32.
            Toast.makeText(this, "Orden de dispensar (prueba)", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnRecordar).setOnClickListener {
            // TEMPORAL: falta reprogramar el aviso a los 5 minutos con AlarmManager.
            Toast.makeText(this, "Te lo recordaremos en 5 minutos", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun aplicarEstado(estado: Estado) {
        val chip = findViewById<LinearLayout>(R.id.chipConexion)
        val ivChip = findViewById<ImageView>(R.id.ivChip)
        val tvChip = findViewById<TextView>(R.id.tvChip)
        val titulo = findViewById<TextView>(R.id.tvTituloAviso)

        when (estado) {
            Estado.PENDIENTE -> {
                chip.setBackgroundResource(R.drawable.bg_chip_conectado)
                ivChip.setColorFilter(Color.parseColor("#0B5D2A"))
                tvChip.text = "Pastillero conectado"
                tvChip.setTextColor(Color.parseColor("#0B5D2A"))
                titulo.text = "Es hora de tu medicamento"
            }
            Estado.PENDIENTE_SIN_CONEXION -> {
                chip.setBackgroundResource(R.drawable.bg_chip_sin_conexion)
                ivChip.setColorFilter(Color.parseColor("#7A4B00"))
                tvChip.text = "Pastillero sin conexión"
                tvChip.setTextColor(Color.parseColor("#7A4B00"))
                titulo.text = "Es hora de tu medicamento"
            }
        }
    }
}