package com.medibox.modelo

import java.time.LocalDateTime

enum class EstadoToma {
    PENDIENTE,
    DISPENSADA,
    TOMADA,
    POSPUESTA,
    NO_CONFIRMADA,
    TARDIA,
    ERROR
}

class Toma(
    var id: Long = 0,
    var horaProgramada: LocalDateTime = LocalDateTime.now(),
    var estado: EstadoToma = EstadoToma.PENDIENTE,
    var venceEn: LocalDateTime = LocalDateTime.now(),
    var dispensadaEn: LocalDateTime? = null,
    var confirmadaEn: LocalDateTime? = null,
    var posposiciones: Int = 0
) {
    companion object {
        const val TIEMPO_ESPERA_MIN: Int = 5
    }

    fun dispensar(instante: LocalDateTime) {
        // TODO: Sin lógica para entrega inicial
    }

    fun posponer(instante: LocalDateTime) {
        // TODO: Sin lógica para entrega inicial
    }

    fun confirmar(instante: LocalDateTime): EstadoToma {
        // TODO: Sin lógica para entrega inicial
        return estado
    }

    fun vencer(instante: LocalDateTime) {
        // TODO: Sin lógica para entrega inicial
    }

    fun marcarError(instante: LocalDateTime) {
        // TODO: Sin lógica para entrega inicial
    }

    fun estaVencida(instante: LocalDateTime): Boolean {
        // TODO: Sin lógica para entrega inicial
        return false
    }
}