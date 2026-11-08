package com.medibox.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.medibox.R
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var horaSeleccionadaTexto: String = "08:00 AM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Vistas de la tarjeta principal
        val tvNombreMed = findViewById<TextView>(R.id.tvNombreMedicamento)
        val tvDosis = findViewById<TextView>(R.id.tvDosis)
        val tvHoraProgramada = findViewById<TextView>(R.id.tvHoraProgramada)
        val tvEstadoAlerta = findViewById<TextView>(R.id.tvEstadoAlerta)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarToma)
        val btnPosponer = findViewById<Button>(R.id.btnPosponerToma)

        // Vistas del formulario de programación
        val etNombreMed = findViewById<TextInputEditText>(R.id.etNombreMedicamento)
        val etDosisMed = findViewById<TextInputEditText>(R.id.etDosisMedicamento)
        val tvHoraSeleccionada = findViewById<TextView>(R.id.tvHoraSeleccionada)
        val btnElegirHora = findViewById<Button>(R.id.btnElegirHora)
        val btnProgramar = findViewById<Button>(R.id.btnProgramarToma)

        // Vistas de vinculación
        val etCodigo = findViewById<TextInputEditText>(R.id.etCodigoVinculacion)
        val btnVincular = findViewById<Button>(R.id.btnVincular)

        // 1. Selector de hora (TimePickerDialog)
        btnElegirHora.setOnClickListener {
            val calendario = Calendar.getInstance()
            val horaActual = calendario.get(Calendar.HOUR_OF_DAY)
            val minutoActual = calendario.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, hora, minuto ->
                val amPm = if (hora >= 12) "PM" else "AM"
                val hora12 = if (hora % 12 == 0) 12 else hora % 12
                horaSeleccionadaTexto = String.format(Locale.getDefault(), "%02d:%02d %s", hora12, minuto, amPm)
                tvHoraSeleccionada.text = "Hora: $horaSeleccionadaTexto"
            }, horaActual, minutoActual, false).show()
        }

        // 2. Programar medicamento (actualiza la tarjeta de arriba)
        btnProgramar.setOnClickListener {
            val nombre = etNombreMed.text.toString().trim()
            val dosis = etDosisMed.text.toString().trim()

            if (nombre.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa el nombre del medicamento", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dosisFinal = if (dosis.isNotEmpty()) dosis else "1 dosis"

            // Actualizar tarjeta superior
            tvNombreMed.text = nombre
            tvDosis.text = "Dosis: $dosisFinal"
            tvHoraProgramada.text = "Hora: $horaSeleccionadaTexto"
            tvEstadoAlerta.text = "PRÓXIMA TOMA PROGRAMADA"
            tvEstadoAlerta.setBackgroundColor(getColor(android.R.color.holo_blue_light))

            // Habilitar botón de toma para el nuevo horario
            btnConfirmar.isEnabled = true
            btnConfirmar.text = "YA TOMÉ EL MEDICAMENTO"

            // Limpiar inputs
            etNombreMed.text?.clear()
            etDosisMed.text?.clear()

            Toast.makeText(this, "Toma programada con éxito", Toast.LENGTH_SHORT).show()
        }

        // 3. Confirmar toma
        btnConfirmar.setOnClickListener {
            Toast.makeText(this, "¡Toma confirmada exitosamente!", Toast.LENGTH_LONG).show()
            btnConfirmar.isEnabled = false
            btnConfirmar.text = "TOMA REGISTRADA"
            tvEstadoAlerta.text = "MEDICAMENTO TOMADO"
        }

        // 4. Posponer 5 minutos
        btnPosponer.setOnClickListener {
            Toast.makeText(this, "Alarma pospuesta 5 minutos.", Toast.LENGTH_SHORT).show()
        }

        // 5. Vincular cuidador
        btnVincular.setOnClickListener {
            val codigo = etCodigo.text.toString().trim()
            if (codigo.length >= 4) {
                Toast.makeText(this, "Vinculado correctamente con código $codigo", Toast.LENGTH_SHORT).show()
                etCodigo.text?.clear()
            } else {
                Toast.makeText(this, "Ingresa un código válido", Toast.LENGTH_SHORT).show()
            }
        }
    }
}