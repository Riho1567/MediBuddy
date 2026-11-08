package com.medibox.modelo

class AdultoMayor : Usuario() {
    fun ingresarCodigo(codigo: String): Vinculacion {
        // TODO: Sin lógica para entrega inicial
        return Vinculacion()
    }

    fun consultarMedicamento(): Medicamento {
        // TODO: Sin lógica para entrega inicial
        return Medicamento()
    }
}
