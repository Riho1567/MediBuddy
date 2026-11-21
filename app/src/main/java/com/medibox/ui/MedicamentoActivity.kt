package com.medibox.ui

import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.medibox.R

class MedicamentoActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private var esCuidador = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicamento)

        prefs = getSharedPreferences("medibuddy", Context.MODE_PRIVATE)
        esCuidador = (intent.getStringExtra("PERFIL") ?: "CUIDADOR") == "CUIDADOR"

        findViewById<MaterialButton>(R.id.btnAgregar).setOnClickListener { mostrarFormulario(false) }
        findViewById<MaterialButton>(R.id.btnEditar).setOnClickListener { mostrarFormulario(true) }
        findViewById<MaterialButton>(R.id.btnEliminar).setOnClickListener { confirmarEliminar() }

        pintar()
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

        // Insignia de perfil y título
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

        // El cuidador edita; el adulto mayor solo consulta.
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

        // Se valida antes de cerrar el cuadro.
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val dosis = etDosis.text.toString().trim()
            if (nombre.isEmpty()) { etNombre.error = "Escribe el nombre"; return@setOnClickListener }
            if (dosis.isEmpty()) { etDosis.error = "Escribe la dosis"; return@setOnClickListener }

            // TEMPORAL: guardado local. Luego se envía al servidor.
            prefs.edit()
                .putString("med_nombre", nombre)
                .putString("med_dosis", dosis)
                .putInt("med_hora", hora)
                .putInt("med_minuto", minuto)
                .apply()
            dialogo.dismiss()
            pintar()
        }
    }

    private fun confirmarEliminar() {
        val nombre = prefs.getString("med_nombre", "") ?: ""
        val hora = formatearHora(prefs.getInt("med_hora", 20), prefs.getInt("med_minuto", 0))
        MaterialAlertDialogBuilder(this)
            .setTitle("¿Eliminar este medicamento?")
            .setMessage("$nombre · $hora. Dejarás de recibir sus avisos. El historial se conserva.")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                prefs.edit()
                    .remove("med_nombre").remove("med_dosis")
                    .remove("med_hora").remove("med_minuto")
                    .apply()
                pintar()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}