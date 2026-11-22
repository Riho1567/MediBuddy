package com.medibox.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.ImageViewCompat
import com.google.android.material.button.MaterialButton
import com.medibox.R
import com.medibox.conexion.Esp32Client
import com.medibox.data.repositorio.MedicamentoRepository
import com.medibox.data.repositorio.RegistroHistorial
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AvisoDosisActivity : AppCompatActivity() {

    enum class Estado {
        PENDIENTE, PENDIENTE_SIN_CONEXION, DISPENSADA,
        TOMADA, POSPUESTA, TARDIA, NO_CONFIRMADA, ERROR
    }

    companion object {
        private const val VENTANA_MS = 5 * 60 * 1000L   // tiempo para confirmar
        private const val CADA_MS = 2000L               // cada cuánto se pregunta al pastillero
    }

    private var estadoActual = Estado.PENDIENTE
    private lateinit var medicamento: String
    private lateinit var dosis: String
    private lateinit var hora: String
    private lateinit var horaRegistro: String
    private lateinit var cliente: Esp32Client

    private val handler = Handler(Looper.getMainLooper())
    private var vigilando = false
    private var inicioVigilancia = 0L

    // Pregunta al pastillero si ya se presionó el botón físico.
    private val vigilancia = object : Runnable {
        override fun run() {
            if (!vigilando) return
            if (System.currentTimeMillis() - inicioVigilancia > VENTANA_MS) {
                vigilando = false
                registrarToma("NO CONFIRMADA")
                aplicarEstado(Estado.NO_CONFIRMADA)
                return
            }
            cliente.estado { ok, cuerpo ->
                if (!vigilando || isFinishing) return@estado
                if (ok && cuerpo.trim() == "tomado") {
                    vigilando = false
                    confirmarToma()
                } else {
                    handler.postDelayed(this, CADA_MS)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aviso_dosis)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        cliente = Esp32Client(this)

        medicamento = intent.getStringExtra("MEDICAMENTO") ?: "Naproxeno"
        dosis = intent.getStringExtra("DOSIS") ?: "1 tableta · 500 mg"
        hora = intent.getStringExtra("HORA") ?: "8:00 p. m."
        horaRegistro = intent.getStringExtra("HORA_REGISTRO") ?: "8:09 p. m."

        // Para probar un estado, pasa "ESTADO" por intent (por ejemplo "TARDIA" o "ERROR").
        val nombre = intent.getStringExtra("ESTADO") ?: "PENDIENTE"

        findViewById<TextView>(R.id.tvMedicamento).text = medicamento
        findViewById<TextView>(R.id.tvDosis).text = dosis

        findViewById<MaterialButton>(R.id.btnDispensar).setOnClickListener { accionPrimaria() }
        findViewById<MaterialButton>(R.id.btnRecordar).setOnClickListener { accionSecundaria() }

        aplicarEstado(Estado.valueOf(nombre))
    }

    override fun onResume() {
        super.onResume()
        verificarConexion()
    }

    override fun onDestroy() {
        vigilando = false
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    // Comprueba si el celular está en la red del pastillero.
    private fun verificarConexion() {
        if (estadoActual != Estado.PENDIENTE && estadoActual != Estado.PENDIENTE_SIN_CONEXION) return
        cliente.status { ok, _ ->
            if (isFinishing || isDestroyed) return@status
            if (estadoActual == Estado.PENDIENTE || estadoActual == Estado.PENDIENTE_SIN_CONEXION) {
                aplicarEstado(if (ok) Estado.PENDIENTE else Estado.PENDIENTE_SIN_CONEXION)
            }
        }
    }

    private fun dispensar() {
        findViewById<MaterialButton>(R.id.btnDispensar).isEnabled = false
        cliente.dispense { ok, _ ->
            if (isFinishing || isDestroyed) return@dispense
            if (ok) {
                aplicarEstado(Estado.DISPENSADA)
                vigilando = true
                inicioVigilancia = System.currentTimeMillis()
                handler.post(vigilancia)
            } else {
                registrarToma("ERROR AL DISPENSAR")
                aplicarEstado(Estado.ERROR)
            }
        }
    }

    private fun confirmarToma() {
        MedicamentoRepository.tomaConfirmada = true
        registrarToma("TOMADO")
        aplicarEstado(Estado.TOMADA)
    }

    private fun accionPrimaria() {
        when (estadoActual) {
            Estado.PENDIENTE -> dispensar()
            Estado.PENDIENTE_SIN_CONEXION -> { /* sin conexión no se puede dispensar */ }
            Estado.DISPENSADA -> {
                // Alternativa al botón físico: confirma desde la app.
                vigilando = false
                cliente.taken { _, _ ->
                    if (!isFinishing && !isDestroyed) confirmarToma()
                }
            }
            else -> irAlInicio()
        }
    }

    private fun accionSecundaria() {
        when (estadoActual) {
            Estado.PENDIENTE, Estado.PENDIENTE_SIN_CONEXION -> {
                // TEMPORAL: falta reprogramar el aviso a los 5 minutos (etapa 2).
                registrarToma("POSPUESTO 5 MIN")
                aplicarEstado(Estado.POSPUESTA)
            }
            Estado.DISPENSADA -> startActivity(Intent(this, HistorialActivity::class.java))
            else -> {}
        }
    }

    private fun registrarToma(estado: String) {
        val horaActual = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        MedicamentoRepository.historial.add(
            0,
            RegistroHistorial(
                medicamento = medicamento,
                dosis = dosis,
                hora = "Hoy - $horaActual",
                estado = estado
            )
        )
    }

    private fun irAlInicio() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra("USUARIO_NOMBRE", intent.getStringExtra("USUARIO_NOMBRE") ?: "Paciente")
            putExtra("PERFIL", intent.getStringExtra("PERFIL") ?: "ADULTO_MAYOR")
        })
        finish()
    }

    private fun aplicarEstado(estado: Estado) {
        estadoActual = estado

        val chip = findViewById<LinearLayout>(R.id.chipConexion)
        val ivChip = findViewById<ImageView>(R.id.ivChip)
        val tvChip = findViewById<TextView>(R.id.tvChip)
        val circulo = findViewById<FrameLayout>(R.id.circuloEstado)
        val ivEstado = findViewById<ImageView>(R.id.ivEstado)
        val titulo = findViewById<TextView>(R.id.tvTituloAviso)
        val detalle = findViewById<TextView>(R.id.tvHora)
        val alerta = findViewById<TextView>(R.id.tvAlerta)
        val btn1 = findViewById<MaterialButton>(R.id.btnDispensar)
        val btn2 = findViewById<MaterialButton>(R.id.btnRecordar)

        val sinConexion = estado == Estado.PENDIENTE_SIN_CONEXION
        chip.setBackgroundResource(
            if (sinConexion) R.drawable.bg_chip_sin_conexion else R.drawable.bg_chip_conectado
        )
        val colorChip = Color.parseColor(if (sinConexion) "#9B1C1C" else "#0B5D2A")
        ImageViewCompat.setImageTintList(ivChip, ColorStateList.valueOf(colorChip))
        tvChip.setTextColor(colorChip)
        tvChip.text = if (sinConexion) "Pastillero sin conexión" else "Pastillero conectado"
        alerta.visibility = if (sinConexion) View.VISIBLE else View.GONE

        val tituloTxt: String
        val colorTitulo: String
        val icono: Int
        val fondo: String
        val trazo: String
        val detalleTxt: String
        val boton1: String
        val boton2: String?

        when (estado) {
            Estado.PENDIENTE, Estado.PENDIENTE_SIN_CONEXION -> {
                tituloTxt = "Es hora de tu medicamento"; colorTitulo = "#0B3A75"
                icono = R.drawable.ic_campana; fondo = "#E8EEF7"; trazo = "#0B3A75"
                detalleTxt = "Hora programada: $hora"
                boton1 = "Dispensar mi medicamento"; boton2 = "Recordármelo en 5 min"
            }
            Estado.DISPENSADA -> {
                tituloTxt = "Medicamento dispensado"; colorTitulo = "#0B3A75"
                icono = R.drawable.ic_pastilla; fondo = "#E8EEF7"; trazo = "#0B3A75"
                detalleTxt = "Presiona el botón del pastillero. Tienes 5 min."
                boton1 = "Ya lo tomé, ir al inicio"; boton2 = "Ver historial"
            }
            Estado.TOMADA -> {
                tituloTxt = "Toma confirmada"; colorTitulo = "#0B5D2A"
                icono = R.drawable.ic_check; fondo = "#E6F4EA"; trazo = "#0B5D2A"
                detalleTxt = "Registrada en el historial"
                boton1 = "Ir al inicio"; boton2 = null
            }
            Estado.POSPUESTA -> {
                tituloTxt = "Aviso pospuesto"; colorTitulo = "#374151"
                icono = R.drawable.ic_reloj; fondo = "#F3F4F6"; trazo = "#374151"
                detalleTxt = "Estado: Pospuesta"
                boton1 = "Ir al inicio"; boton2 = null
            }
            Estado.TARDIA -> {
                tituloTxt = "Toma tardía"; colorTitulo = "#7A4B00"
                icono = R.drawable.ic_check; fondo = "#FFF4D6"; trazo = "#7A4B00"
                detalleTxt = "Registrada a las $horaRegistro"
                boton1 = "Ir al inicio"; boton2 = null
            }
            Estado.NO_CONFIRMADA -> {
                tituloTxt = "Toma no confirmada"; colorTitulo = "#9B1C1C"
                icono = R.drawable.ic_campana_tachada; fondo = "#FDECEC"; trazo = "#9B1C1C"
                detalleTxt = "Hora programada: $hora"
                boton1 = "Ir al inicio"; boton2 = null
            }
            Estado.ERROR -> {
                tituloTxt = "No se pudo dispensar"; colorTitulo = "#5B21B6"
                icono = R.drawable.ic_equis; fondo = "#F1EAFE"; trazo = "#5B21B6"
                detalleTxt = "Estado: Error"
                boton1 = "Ir al inicio"; boton2 = null
            }
        }

        titulo.text = tituloTxt
        titulo.setTextColor(Color.parseColor(colorTitulo))
        detalle.text = detalleTxt

        ivEstado.setImageResource(icono)
        ImageViewCompat.setImageTintList(ivEstado, ColorStateList.valueOf(Color.parseColor(trazo)))
        val fondoCirculo = circulo.background.mutate() as GradientDrawable
        fondoCirculo.setColor(Color.parseColor(fondo))
        fondoCirculo.setStroke((3 * resources.displayMetrics.density).toInt(), Color.parseColor(trazo))

        btn1.text = boton1
        btn1.isEnabled = estado != Estado.PENDIENTE_SIN_CONEXION
        if (boton2 == null) {
            btn2.visibility = View.GONE
        } else {
            btn2.visibility = View.VISIBLE
            btn2.text = boton2
        }
    }
}