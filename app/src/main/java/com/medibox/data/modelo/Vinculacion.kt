package com.medibox.modelo

import java.time.LocalDateTime

class Vinculacion(
    var id: Long = 0,
    var fechaVinculacion: LocalDateTime = LocalDateTime.now()
)