package com.medibox.modelo

import java.time.LocalDateTime
import java.time.LocalTime

class Medicamento(
    var id: Long = 0,
    var nombre: String = "",
    var dosis: String = "",
    var horaToma: LocalTime = LocalTime.now(),
    var compartimento: Int = 1,
    var activo: Boolean = true,
    var fechaCreacion: LocalDateTime = LocalDateTime.now()
) {
    fun editar(nombre: String, dosis: String, hora: LocalTime) {
        // TODO: Sin lógica para entrega inicial
    }

    fun desactivar() {
        // TODO: Sin lógica para entrega inicial
    }

    fun proximaToma(desde: LocalDateTime): LocalDateTime {
        // TODO: Sin lógica para entrega inicial
        return LocalDateTime.now()
    }
}