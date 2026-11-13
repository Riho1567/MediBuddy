package com.medibox.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.medibox.R
import com.medibox.data.repositorio.MedicamentoRepository

class HistorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        val btnVolver = findViewById<Button>(R.id.btnVolverDesdeHistorial)
        val contenedor = findViewById<LinearLayout>(R.id.contenedorHistorial)

        btnVolver.setOnClickListener { finish() }

        for (item in MedicamentoRepository.historial) {
            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 28
                }
                radius = 32f
                cardElevation = 4f
                setCardBackgroundColor(Color.WHITE)
            }

            val fila = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(42, 36, 42, 36)
            }

            val tvEstado = TextView(this).apply {
                text = item.estado
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                setTextColor(
                    if (item.estado == "TOMADO") Color.parseColor("#2E7D32")
                    else Color.parseColor("#EF6C00")
                )
            }

            val tvMed = TextView(this).apply {
                text = "${item.medicamento} (${item.dosis})"
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#102027"))
            }

            val tvHora = TextView(this).apply {
                text = "Registrado: ${item.hora}"
                textSize = 14f
                setTextColor(Color.parseColor("#546E7A"))
            }

            fila.addView(tvEstado)
            fila.addView(tvMed)
            fila.addView(tvHora)
            card.addView(fila)
            contenedor.addView(card)
        }
    }
}