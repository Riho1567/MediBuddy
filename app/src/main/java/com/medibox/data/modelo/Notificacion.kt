package com.medibox.modelo

import java.time.LocalDateTime

enum class TipoNotificacion {
    TOMA_CONFIRMADA,
    NO_CONFIRMADA,
    ERROR_PASTILLERO
}

class Notificacion(
    var id: Long = 0,
    var tipo: TipoNotificacion = TipoNotificacion.TOMA_CONFIRMADA,
    var mensaje: String = "",
    var creadaEn: LocalDateTime = LocalDateTime.now(),
    var entregada: Boolean = false
) {
    fun marcarEntregada() {
        // TODO: Sin lógica para entrega inicial
    }
}