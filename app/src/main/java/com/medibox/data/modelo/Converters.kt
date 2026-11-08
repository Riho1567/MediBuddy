package com.medibox.data.modelo

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class Rol {
    ADULTO_MAYOR,
    CUIDADOR
}

enum class EstadoToma {
    PENDIENTE,
    DISPENSADA,
    TOMADA,
    POSPUESTA,
    NO_CONFIRMADA,
    TARDIA,
    ERROR
}

enum class TipoNotificacion {
    TOMA_CONFIRMADA,
    NO_CONFIRMADA,
    ERROR_PASTILLERO
}

class Converters {
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.format(timeFormatter)

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it, timeFormatter) }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.format(dateTimeFormatter)

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
}