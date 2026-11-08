package com.medibox.data.repositorio

import com.medibox.data.datos.MediBoxDao
import com.medibox.data.modelo.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

class MediBoxRepository(private val dao: MediBoxDao) {

    suspend fun generarCodigoVinculacion(cuidadorId: Long): CodigoVinculacion {
        val codigoTexto = UUID.randomUUID().toString().take(6).uppercase()
        val codigo = CodigoVinculacion(cuidadorId = cuidadorId, codigo = codigoTexto)
        val id = dao.insertCodigoVinculacion(codigo)
        return codigo.copy(id = id)
    }

    suspend fun vincularAdultoMayor(adultoMayorId: Long, codigoTexto: String): Boolean {
        val codigo = dao.getCodigoActivo(codigoTexto) ?: return false
        if (!codigo.esValido()) return false

        codigo.marcarUsado()
        dao.updateCodigoVinculacion(codigo)

        val vinculacion = Vinculacion(
            cuidadorId = codigo.cuidadorId,
            adultoMayorId = adultoMayorId,
            codigoVinculacionId = codigo.id
        )
        dao.insertVinculacion(vinculacion)
        return true
    }

    suspend fun programarMedicamento(
        cuidadorId: Long,
        adultoMayorId: Long,
        nombre: String,
        dosis: String,
        hora: LocalTime
    ): Medicamento {
        val med = Medicamento(
            cuidadorId = cuidadorId,
            adultoMayorId = adultoMayorId,
            nombre = nombre,
            dosis = dosis,
            horaToma = hora
        )
        val medId = dao.insertMedicamento(med)
        med.id = medId

        val proximaFecha = med.proximaToma()
        val toma = Toma(
            medicamentoId = medId,
            adultoMayorId = adultoMayorId,
            horaProgramada = proximaFecha
        )
        val tomaId = dao.insertToma(toma)

        dao.insertRegistroToma(
            RegistroToma(
                tomaId = tomaId,
                estado = EstadoToma.PENDIENTE,
                detalle = "Toma programada creada"
            )
        )
        return med
    }

    suspend fun confirmarToma(toma: Toma, cuidadorId: Long) {
        val nuevoEstado = toma.confirmar(LocalDateTime.now())
        dao.updateToma(toma)

        dao.insertRegistroToma(
            RegistroToma(
                tomaId = toma.id,
                estado = nuevoEstado,
                detalle = "Toma confirmada por el usuario"
            )
        )

        dao.insertNotificacion(
            Notificacion(
                tomaId = toma.id,
                destinatarioId = cuidadorId,
                tipo = TipoNotificacion.TOMA_CONFIRMADA,
                mensaje = "El adulto mayor registró la toma con estado: $nuevoEstado"
            )
        )
    }

    suspend fun posponerToma(toma: Toma) {
        toma.posponer(LocalDateTime.now())
        dao.updateToma(toma)

        dao.insertRegistroToma(
            RegistroToma(
                tomaId = toma.id,
                estado = EstadoToma.POSPUESTA,
                detalle = "Postergada 5 minutos"
            )
        )
    }
}