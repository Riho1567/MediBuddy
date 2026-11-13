package com.medibox.ui


import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.medibox.R
import com.medibox.data.repositorio.MedicamentoRepository
import java.util.Calendar
import java.util.Locale

class RegistroActivity : AppCompatActivity() {

    private var horaSeleccionadaTexto: String = "08:00 AM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val btnVolver = findViewById<Button>(R.id.btnVolverDesdeRegistro)
        val etNombreMed = findViewById<TextInputEditText>(R.id.etNombreMedicamento)
        val etDosisMed = findViewById<TextInputEditText>(R.id.etDosisMedicamento)
        val tvHoraSeleccionada = findViewById<TextView>(R.id.tvHoraSeleccionada)
        val btnElegirHora = findViewById<Button>(R.id.btnElegirHora)
        val btnProgramar = findViewById<Button>(R.id.btnProgramarToma)
        val etCodigo = findViewById<TextInputEditText>(R.id.etCodigoVinculacion)
        val btnVincular = findViewById<Button>(R.id.btnVincular)

        btnVolver.setOnClickListener { finish() }

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

        btnProgramar.setOnClickListener {
            val nombre = etNombreMed.text.toString().trim()
            val dosis = etDosisMed.text.toString().trim()

            if (nombre.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa el nombre del medicamento", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            MedicamentoRepository.medicamentoActual = nombre
            MedicamentoRepository.dosisActual = dosis.ifEmpty { "1 dosis" }
            MedicamentoRepository.horaActual = horaSeleccionadaTexto
            MedicamentoRepository.tomaConfirmada = false

            Toast.makeText(this, "Toma programada con éxito", Toast.LENGTH_SHORT).show()
            finish()
        }

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