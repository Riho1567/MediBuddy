package com.medibox.modelo

import java.time.LocalDateTime

class CodigoVinculacion(
    var id: Long = 0,
    var codigo: String = "",
    var creadoEn: LocalDateTime = LocalDateTime.now(),
    var expiraEn: LocalDateTime = LocalDateTime.now(),
    var usado: Boolean = false
) {
    fun esValido(instante: LocalDateTime): Boolean {
        // TODO: Sin lógica para entrega inicial
        return false
    }

    fun marcarUsado() {
        // TODO: Sin lógica para entrega inicial
    }
}