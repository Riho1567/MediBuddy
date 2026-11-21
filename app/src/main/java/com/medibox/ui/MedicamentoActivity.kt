package com.medibox.ui

import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.medibox.R
import com.medibox.network.RetrofitClient
import com.medibox.network.SessionManager
import com.medibox.network.model.CreateScheduleDto
import com.medibox.network.model.UpdateScheduleDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicamentoActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var sessionManager: SessionManager
    private var esCuidador = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicamento)

        prefs = getSharedPreferences("medibuddy", Context.MODE_PRIVATE)
        sessionManager = SessionManager(this)

        val perfilParam = intent.getStringExtra("PERFIL") ?: sessionManager.getUserRole()
        esCuidador = perfilParam == "CUIDADOR"

        findViewById<MaterialButton>(R.id.btnAgregar).setOnClickListener { mostrarFormulario(false) }
        findViewById<MaterialButton>(R.id.btnEditar).setOnClickListener { mostrarFormulario(true) }
        findViewById<MaterialButton>(R.id.btnEliminar).setOnClickListener { confirmarEliminar() }

        pintar()
        sincronizarConServidor()
    }

    private fun sincronizarConServidor() {
        val targetDeviceId = sessionManager.getLinkedDeviceId() ?: sessionManager.getDeviceId()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val api = RetrofitClient.getDeviceApi(this@MedicamentoActivity)
                val res = api.getSchedules(targetDeviceId)
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    val activeSchedule = res.body()!!.firstOrNull { it.isActive }
                    if (activeSchedule != null) {
                        prefs.edit()
                            .putLong("med_id", activeSchedule.id ?: 0L)
                            .putString("med_nombre", activeSchedule.medicationName)
                            .putString("med_dosis", activeSchedule.dose)
                            .putInt("med_hora", activeSchedule.scheduledHour)
                            .putInt("med_minuto", activeSchedule.scheduledMinute)
                            .apply()

                        withContext(Dispatchers.Main) {
                            pintar()
                        }
                    }
                }
            } catch (e: Exception) {
                // Silencioso en sincronización pasiva
            }
        }
    }

    private fun tieneMedicamento() = prefs.getString("med_nombre", null) != null

    private fun formatearHora(h: Int, m: Int): String {
        val sufijo = if (h < 12) "a. m." else "p. m."
        val h12 = if (h % 12 == 0) 12 else h % 12
        return "$h12:${m.toString().padStart(2, '0')} $sufijo"
    }

    private fun pintar() {
        val chip = findViewById<TextView>(R.id.tvChipPerfil)
        val titulo = findViewById<TextView>(R.id.tvTituloMed)
        val tarjeta = findViewById<View>(R.id.tarjetaMed)
        val vacio = findViewById<TextView>(R.id.tvVacio)
        val aviso = findViewById<View>(R.id.avisoAdulto)
        val fila = findViewById<View>(R.id.filaBotones)
        val btnAgregar = findViewById<View>(R.id.btnAgregar)

        if (esCuidador) {
            chip.text = "Cuidador"
            chip.setBackgroundResource(R.drawable.bg_chip_cuidador)
            chip.setTextColor(Color.parseColor("#0B4F9E"))
            val adulto = intent.getStringExtra("NOMBRE_ADULTO") ?: "tu familiar"
            titulo.text = "Medicamento de $adulto"
        } else {
            chip.text = "Adulto mayor"
            chip.setBackgroundResource(R.drawable.bg_chip_adulto)
            chip.setTextColor(Color.parseColor("#7A4B00"))
            titulo.text = "Mi medicamento"
        }

        val hay = tieneMedicamento()
        tarjeta.visibility = if (hay) View.VISIBLE else View.GONE
        vacio.visibility = if (hay) View.GONE else View.VISIBLE
        vacio.text = if (esCuidador) "Aún no hay un medicamento registrado."
        else "Tu cuidador todavía no registra tu medicamento."

        if (hay) {
            val h = prefs.getInt("med_hora", 20)
            val m = prefs.getInt("med_minuto", 0)
            findViewById<TextView>(R.id.tvNombreMed).text = prefs.getString("med_nombre", "")
            findViewById<TextView>(R.id.tvDosisMed).text = "Dosis: " + prefs.getString("med_dosis", "")
            findViewById<TextView>(R.id.tvHoraMed).text = "Hora diaria: " + formatearHora(h, m)
        }

        fila.visibility = if (esCuidador && hay) View.VISIBLE else View.GONE
        btnAgregar.visibility = if (esCuidador && !hay) View.VISIBLE else View.GONE
        aviso.visibility = if (!esCuidador && hay) View.VISIBLE else View.GONE
    }

    private fun mostrarFormulario(editar: Boolean) {
        val vista = LayoutInflater.from(this).inflate(R.layout.dialog_medicamento, null)
        val etNombre = vista.findViewById<TextInputEditText>(R.id.etNombreMed)
        val etDosis = vista.findViewById<TextInputEditText>(R.id.etDosisMed)
        val btnHora = vista.findViewById<MaterialButton>(R.id.btnHoraMed)

        var hora = if (editar) prefs.getInt("med_hora", 20) else 20
        var minuto = if (editar) prefs.getInt("med_minuto", 0) else 0

        if (editar) {
            etNombre.setText(prefs.getString("med_nombre", ""))
            etDosis.setText(prefs.getString("med_dosis", ""))
        }
        btnHora.text = "Hora diaria: " + formatearHora(hora, minuto)
        btnHora.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                hora = h; minuto = m
                btnHora.text = "Hora diaria: " + formatearHora(h, m)
            }, hora, minuto, false).show()
        }

        val dialogo: AlertDialog = MaterialAlertDialogBuilder(this)
            .setTitle(if (editar) "Editar medicamento" else "Agregar medicamento")
            .setView(vista)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()
        dialogo.show()

        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val dosis = etDosis.text.toString().trim()
            if (nombre.isEmpty()) { etNombre.error = "Escribe el nombre"; return@setOnClickListener }
            if (dosis.isEmpty()) { etDosis.error = "Escribe la dosis"; return@setOnClickListener }

            // Guardado local inmediato
            prefs.edit()
                .putString("med_nombre", nombre)
                .putString("med_dosis", dosis)
                .putInt("med_hora", hora)
                .putInt("med_minuto", minuto)
                .apply()
            dialogo.dismiss()
            pintar()

            // Enviar a la API
            val targetDeviceId = sessionManager.getLinkedDeviceId() ?: sessionManager.getDeviceId()
            val scheduleId = prefs.getLong("med_id", -1L)

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val api = RetrofitClient.getDeviceApi(this@MedicamentoActivity)
                    if (editar && scheduleId > 0) {
                        api.updateSchedule(
                            deviceId = targetDeviceId,
                            scheduleId = scheduleId,
                            req = UpdateScheduleDto(
                                medicationName = nombre,
                                dose = dosis,
                                scheduledHour = hora,
                                scheduledMinute = minuto,
                                clientType = "CUIDADOR"
                            )
                        )
                    } else {
                        val res = api.createSchedule(
                            deviceId = targetDeviceId,
                            req = CreateScheduleDto(
                                medicationName = nombre,
                                dose = dosis,
                                compartment = 1,
                                scheduledHour = hora,
                                scheduledMinute = minuto
                            )
                        )
                        if (res.isSuccessful && res.body()?.id != null) {
                            prefs.edit().putLong("med_id", res.body()!!.id!!).apply()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MedicamentoActivity, "Guardado local. Sincronizará con servidor al reconectar.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun confirmarEliminar() {
        val nombre = prefs.getString("med_nombre", "") ?: ""
        val hora = formatearHora(prefs.getInt("med_hora", 20), prefs.getInt("med_minuto", 0))
        val scheduleId = prefs.getLong("med_id", -1L)
        val targetDeviceId = sessionManager.getLinkedDeviceId() ?: sessionManager.getDeviceId()

        MaterialAlertDialogBuilder(this)
            .setTitle("¿Eliminar este medicamento?")
            .setMessage("$nombre · $hora. Dejarás de recibir sus avisos. El historial se conserva.")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                prefs.edit()
                    .remove("med_nombre").remove("med_dosis")
                    .remove("med_hora").remove("med_minuto")
                    .remove("med_id")
                    .apply()
                pintar()

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        if (scheduleId > 0) {
                            RetrofitClient.getDeviceApi(this@MedicamentoActivity)
                                .deleteSchedule(targetDeviceId, scheduleId, "CUIDADOR")
                        }
                    } catch (e: Exception) {
                        // Modo offline
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
