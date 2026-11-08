package com.medibox.modelo

import java.time.LocalDateTime

enum class Rol {
    ADULTO_MAYOR,
    CUIDADOR
}

open class Usuario(
    var id: Long = 0,
    var nombre: String = "",
    var correo: String = "",
    var contrasenaHash: String = "",
    var rol: Rol = Rol.ADULTO_MAYOR,
    var fechaRegistro: LocalDateTime = LocalDateTime.now()
) {
    fun verificarContrasena(plana: String): Boolean {
        // TODO: Sin lógica para entrega inicial
        return false
    }
}