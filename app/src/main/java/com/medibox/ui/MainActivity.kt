package com.medibox.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.medibox.R
import com.medibox.data.repositorio.MedicamentoRepository
import com.medibox.network.SessionManager
import com.medibox.network.WebSocketManager
import com.medibox.servicios.AlarmScheduler
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private var esCuidador = false
    private var usuario = "Paciente"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("medibuddy", Context.MODE_PRIVATE)

        // El perfil viaja desde el registro. Si el login no lo envía, se usa el guardado.
        val perfil = intent.getStringExtra("PERFIL")
            ?: prefs.getString("perfil", "ADULTO_MAYOR")
        esCuidador = perfil == "CUIDADOR"
        usuario = intent.getStringExtra("USUARIO_NOMBRE")
            ?: prefs.getString("nombre", "Paciente") ?: "Paciente"

        findViewById<TextView>(R.id.tvSaludo).text = "Hola, $usuario"

        val sessionManager = SessionManager(this)
        val wsManager = WebSocketManager.getInstance(this)
        wsManager.connect()

        // Escuchar eventos WebSocket en segundo plano
        lifecycleScope.launch {
            wsManager.eventsFlow.collectLatest { msg ->
                if (msg.type == "ALERT") {
                    val alertType = msg.payload["alertType"] as? String
                    if (alertType == "MEDICATION_TAKEN") {
                        Toast.makeText(this@MainActivity, "🔔 Pastillero: Medicamento tomado", Toast.LENGTH_LONG).show()
                    } else if (alertType == "LOW_BATTERY") {
                        Toast.makeText(this@MainActivity, "⚠️ Batería baja en pastillero", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Volver al login
        findViewById<MaterialButton>(R.id.btnCerrarSesion).setOnClickListener {
            sessionManager.clearSession()
            wsManager.disconnect()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Barra inferior
        findViewById<View>(R.id.navHistorial).setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }
        findViewById<View>(R.id.navMedicina).setOnClickListener { irAMedicina() }

        // Botón principal según el perfil
        val btnPrincipal = findViewById<MaterialButton>(R.id.btnPrincipal)
        if (esCuidador) {
            btnPrincipal.setOnClickListener { irAMedicina() }
        } else {
            btnPrincipal.text = "Ver historial"
            btnPrincipal.setOnClickListener {
                startActivity(Intent(this, HistorialActivity::class.java))
            }
        }

        // TEMPORAL: abre el aviso de dosis para probarlo.
        findViewById<MaterialButton>(R.id.btnProbarAviso).setOnClickListener {
            startActivity(Intent(this, AvisoDosisActivity::class.java).apply {
                putExtra("MEDICAMENTO", nombreMedicamento())
                putExtra("DOSIS", dosisMedicamento())
                putExtra("HORA", horaMedicamento())
                putExtra("PERFIL", perfil)
                putExtra("USUARIO_NOMBRE", usuario)
            })
        }

        findViewById<View>(R.id.tarjetaVinculado).visibility =
            if (esCuidador) View.VISIBLE else View.GONE
        val linkedId = sessionManager.getLinkedDeviceId()
        findViewById<TextView>(R.id.tvVinculado).text = if (linkedId != null) {
            "Dispositivo vinculado: $linkedId"
        } else {
            "Aún no hay un adulto mayor vinculado."
        }
    }

    override fun onResume() {
        super.onResume()
        pintarToma()
        AlarmScheduler.programarDesdePrefs(this)
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 10)
        }
    }

    private fun irAMedicina() {
        startActivity(Intent(this, MedicamentoActivity::class.java).apply {
            putExtra("PERFIL", if (esCuidador) "CUIDADOR" else "ADULTO_MAYOR")
        })
    }

    // Se lee primero el medicamento de la pantalla Medicina; si no hay, el de MedicamentoRepository.
    private fun hayMedicamentoGuardado() = prefs.getString("med_nombre", null) != null

    private fun nombreMedicamento(): String =
        prefs.getString("med_nombre", null) ?: "${MedicamentoRepository.medicamentoActual}"

    private fun dosisMedicamento(): String =
        prefs.getString("med_dosis", null) ?: "${MedicamentoRepository.dosisActual}"

    private fun horaMedicamento(): String {
        if (!hayMedicamentoGuardado()) return "${MedicamentoRepository.horaActual}"
        val h = prefs.getInt("med_hora", 20)
        val m = prefs.getInt("med_minuto", 0)
        val sufijo = if (h < 12) "a. m." else "p. m."
        val h12 = if (h % 12 == 0) 12 else h % 12
        return "$h12:${m.toString().padStart(2, '0')} $sufijo"
    }

    private fun pintarToma() {
        findViewById<TextView>(R.id.tvNombreMedicamento).text = nombreMedicamento()
        findViewById<TextView>(R.id.tvDosisHora).text =
            "${dosisMedicamento()} · ${horaMedicamento()}"

        val chip = findViewById<TextView>(R.id.tvEstadoToma)
        val detalle = findViewById<TextView>(R.id.tvDetalleEstado)

        val tomada = MedicamentoRepository.tomaConfirmada
        val fondo = if (tomada) "#E6F4EA" else "#E8EEF7"
        val color = if (tomada) "#0B5D2A" else "#0B3A75"

        chip.text = if (tomada) "Tomada" else "Pendiente"
        chip.setTextColor(Color.parseColor(color))
        chip.background = GradientDrawable().apply {
            cornerRadius = 12 * resources.displayMetrics.density
            setColor(Color.parseColor(fondo))
            setStroke((2 * resources.displayMetrics.density).toInt(), Color.parseColor(color))
        }
        detalle.text = if (tomada) "Toma registrada en el historial."
        else "Hora programada: ${horaMedicamento()}"

        // Botón principal del cuidador
        if (esCuidador) {
            findViewById<MaterialButton>(R.id.btnPrincipal).text =
                if (hayMedicamentoGuardado()) "Editar medicamento" else "Agregar medicamento"
        }
    }
}