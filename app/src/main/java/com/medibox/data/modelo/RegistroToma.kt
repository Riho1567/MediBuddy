package com.medibox.modelo

import java.time.LocalDateTime

class RegistroToma(
    val id: Long = 0,
    val estado: EstadoToma = EstadoToma.PENDIENTE,
    val fechaHora: LocalDateTime = LocalDateTime.now(),
    val detalle: String? = null
)