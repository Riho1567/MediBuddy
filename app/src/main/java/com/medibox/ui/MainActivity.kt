package com.medibox.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.medibox.R
import com.medibox.data.repositorio.MedicamentoRepository
import com.medibox.data.repositorio.RegistroHistorial
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvNombreMed: TextView
    private lateinit var tvDosis: TextView
    private lateinit var tvHoraProgramada: TextView
    private lateinit var tvEstadoAlerta: TextView
    private lateinit var btnConfirmar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvSubtitulo = findViewById<TextView>(R.id.tvSubtitulo)
        val usuario = intent.getStringExtra("USUARIO_NOMBRE")
        if (!usuario.isNullOrEmpty()) {
            tvSubtitulo.text = "Hola, $usuario • Compartimento 1"
        }

        tvNombreMed = findViewById(R.id.tvNombreMedicamento)
        tvDosis = findViewById(R.id.tvDosis)
        tvHoraProgramada = findViewById(R.id.tvHoraProgramada)
        tvEstadoAlerta = findViewById(R.id.tvEstadoAlerta)
        btnConfirmar = findViewById(R.id.btnConfirmarToma)

        val btnPosponer = findViewById<Button>(R.id.btnPosponerToma)
        val btnIrRegistro = findViewById<Button>(R.id.btnIrRegistro)
        val btnIrHistorial = findViewById<Button>(R.id.btnIrHistorial)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)

        // Navegación a Registro de Medicamento
        btnIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        // Navegación a Historial
        btnIrHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        // Volver al Login
        btnCerrarSesion.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Confirmar toma y guardar en historial
        btnConfirmar.setOnClickListener {
            val horaActual = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            MedicamentoRepository.tomaConfirmada = true
            MedicamentoRepository.historial.add(
                0,
                RegistroHistorial(
                    medicamento = MedicamentoRepository.medicamentoActual,
                    dosis = MedicamentoRepository.dosisActual,
                    hora = "Hoy - $horaActual",
                    estado = "TOMADO"
                )
            )

            actualizarVistaToma()
            Toast.makeText(this, "¡Toma guardada en el historial!", Toast.LENGTH_LONG).show()
        }

        // Posponer 5 minutos y registrar evento en historial
        btnPosponer.setOnClickListener {
            val horaActual = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            MedicamentoRepository.historial.add(
                0,
                RegistroHistorial(
                    medicamento = MedicamentoRepository.medicamentoActual,
                    dosis = MedicamentoRepository.dosisActual,
                    hora = "Hoy - $horaActual",
                    estado = "POSPUESTO 5 MIN"
                )
            )
            Toast.makeText(this, "Alarma pospuesta 5 minutos.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarVistaToma()
    }

    private fun actualizarVistaToma() {
        tvNombreMed.text = MedicamentoRepository.medicamentoActual
        tvDosis.text = "Dosis: ${MedicamentoRepository.dosisActual}"
        tvHoraProgramada.text = "Hora: ${MedicamentoRepository.horaActual}"

        if (MedicamentoRepository.tomaConfirmada) {
            btnConfirmar.isEnabled = false
            btnConfirmar.text = "TOMA REGISTRADA"
            tvEstadoAlerta.text = "MEDICAMENTO TOMADO"
            tvEstadoAlerta.setBackgroundColor(getColor(android.R.color.holo_green_light))
        } else {
            btnConfirmar.isEnabled = true
            btnConfirmar.text = "YA TOMÉ EL MEDICAMENTO"
            tvEstadoAlerta.text = "PRÓXIMA TOMA PROGRAMADA"
            tvEstadoAlerta.setBackgroundColor(getColor(android.R.color.holo_blue_light))
        }
    }
}