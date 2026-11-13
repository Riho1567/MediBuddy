package com.medibox.data.repositorio


data class RegistroHistorial(
    val medicamento: String,
    val dosis: String,
    val hora: String,
    val estado: String
)

object MedicamentoRepository {
    var medicamentoActual: String = "Losartán 50mg"
    var dosisActual: String = "1 Tableta"
    var horaActual: String = "08:00 AM"
    var tomaConfirmada: Boolean = false

    val historial: MutableList<RegistroHistorial> = mutableListOf(
        RegistroHistorial("Losartán 50mg", "1 Tableta", "Ayer - 08:02 AM", "TOMADO"),
        RegistroHistorial("Metformina 850mg", "1 Tableta", "Ayer - 01:15 PM", "TOMADO")
    )
}