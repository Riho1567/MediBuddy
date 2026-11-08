package com.medibox.data.modelo

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(tableName = "usuarios")
open class Usuario(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var nombre: String,
    var correo: String,
    var contrasenaHash: String,
    var rol: Rol,
    var fechaRegistro: LocalDateTime = LocalDateTime.now()
) {
    fun verificarContrasena(plana: String): Boolean = contrasenaHash == plana
}

@Entity(
    tableName = "codigos_vinculacion",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["cuidadorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cuidadorId")]
)
data class CodigoVinculacion(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val cuidadorId: Long,
    val codigo: String,
    val creadoEn: LocalDateTime = LocalDateTime.now(),
    val expiraEn: LocalDateTime = LocalDateTime.now().plusHours(24),
    var usado: Boolean = false
) {
    fun esValido(instante: LocalDateTime = LocalDateTime.now()): Boolean = !usado && instante.isBefore(expiraEn)
    fun marcarUsado() { usado = true }
}

@Entity(
    tableName = "vinculaciones",
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["cuidadorId"]),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["adultoMayorId"]),
        ForeignKey(entity = CodigoVinculacion::class, parentColumns = ["id"], childColumns = ["codigoVinculacionId"])
    ],
    indices = [Index("cuidadorId"), Index("adultoMayorId"), Index("codigoVinculacionId")]
)
data class Vinculacion(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val cuidadorId: Long,
    val adultoMayorId: Long,
    val codigoVinculacionId: Long,
    val fechaVinculacion: LocalDateTime = LocalDateTime.now()
)

@Entity(
    tableName = "medicamentos",
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["adultoMayorId"]),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["cuidadorId"])
    ],
    indices = [Index("adultoMayorId"), Index("cuidadorId")]
)
data class Medicamento(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val adultoMayorId: Long,
    val cuidadorId: Long,
    var nombre: String,
    var dosis: String,
    var horaToma: LocalTime,
    var compartimento: Int = 1,
    var activo: Boolean = true,
    val fechaCreacion: LocalDateTime = LocalDateTime.now()
) {
    fun editar(nombre: String, dosis: String, hora: LocalTime) {
        this.nombre = nombre
        this.dosis = dosis
        this.horaToma = hora
    }

    fun desactivar() {
        this.activo = false
    }

    fun proximaToma(desde: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        val tomaHoy = desde.toLocalDate().atTime(horaToma)
        return if (desde.isBefore(tomaHoy)) tomaHoy else tomaHoy.plusDays(1)
    }
}

@Entity(
    tableName = "tomas",
    foreignKeys = [
        ForeignKey(entity = Medicamento::class, parentColumns = ["id"], childColumns = ["medicamentoId"]),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["adultoMayorId"])
    ],
    indices = [Index("medicamentoId"), Index("adultoMayorId")]
)
data class Toma(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val medicamentoId: Long,
    val adultoMayorId: Long,
    var horaProgramada: LocalDateTime,
    var estado: EstadoToma = EstadoToma.PENDIENTE,
    var venceEn: LocalDateTime = horaProgramada.plusMinutes(TIEMPO_ESPERA_MIN.toLong()),
    var dispensadaEn: LocalDateTime? = null,
    var confirmadaEn: LocalDateTime? = null,
    var posposiciones: Int = 0
) {
    companion object {
        const val TIEMPO_ESPERA_MIN: Int = 5
    }

    fun dispensar(instante: LocalDateTime = LocalDateTime.now()) {
        dispensadaEn = instante
        estado = EstadoToma.DISPENSADA
    }

    fun posponer(instante: LocalDateTime = LocalDateTime.now()) {
        posposiciones++
        horaProgramada = instante.plusMinutes(5)
        venceEn = horaProgramada.plusMinutes(TIEMPO_ESPERA_MIN.toLong())
        estado = EstadoToma.POSPUESTA
    }

    fun confirmar(instante: LocalDateTime = LocalDateTime.now()): EstadoToma {
        confirmadaEn = instante
        estado = if (instante.isAfter(venceEn)) EstadoToma.TARDIA else EstadoToma.TOMADA
        return estado
    }

    fun vencer(instante: LocalDateTime = LocalDateTime.now()) {
        if (estaVencida(instante) && estado != EstadoToma.TOMADA) {
            estado = EstadoToma.NO_CONFIRMADA
        }
    }

    fun marcarError(instante: LocalDateTime = LocalDateTime.now()) {
        estado = EstadoToma.ERROR
    }

    fun estaVencida(instante: LocalDateTime = LocalDateTime.now()): Boolean = instante.isAfter(venceEn)
}

@Entity(
    tableName = "registros_toma",
    foreignKeys = [
        ForeignKey(entity = Toma::class, parentColumns = ["id"], childColumns = ["tomaId"])
    ],
    indices = [Index("tomaId")]
)
data class RegistroToma(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tomaId: Long,
    val estado: EstadoToma,
    val fechaHora: LocalDateTime = LocalDateTime.now(),
    val detalle: String? = null
)

@Entity(
    tableName = "notificaciones",
    foreignKeys = [
        ForeignKey(entity = Toma::class, parentColumns = ["id"], childColumns = ["tomaId"]),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["destinatarioId"])
    ],
    indices = [Index("tomaId"), Index("destinatarioId")]
)
data class Notificacion(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val tomaId: Long,
    val destinatarioId: Long,
    val tipo: TipoNotificacion,
    val mensaje: String,
    val creadaEn: LocalDateTime = LocalDateTime.now(),
    var entregada: Boolean = false
) {
    fun marcarEntregada() {
        entregada = true
    }
}