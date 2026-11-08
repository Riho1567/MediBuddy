package com.medibox.modelo

import java.time.LocalTime

class Cuidador : Usuario() {
    fun generarCodigoVinculacion(): CodigoVinculacion {
        // TODO: Sin lógica para entrega inicial
        return CodigoVinculacion()
    }

    fun programarMedicamento(nombre: String, dosis: String, hora: LocalTime): Medicamento {
        // TODO: Sin lógica para entrega inicial
        return Medicamento()
    }

    fun editarMedicamento(m: Medicamento, nombre: String, dosis: String, hora: LocalTime) {
        // TODO: Sin lógica para entrega inicial
    }

    fun eliminarMedicamento(m: Medicamento) {
        // TODO: Sin lógica para entrega inicial
    }
}