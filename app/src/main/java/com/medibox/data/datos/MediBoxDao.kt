package com.medibox.data.datos



import androidx.room.*
import com.medibox.data.modelo.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MediBoxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun getUsuarioPorCorreo(correo: String): Usuario?

    @Insert
    suspend fun insertCodigoVinculacion(codigo: CodigoVinculacion): Long

    @Query("SELECT * FROM codigos_vinculacion WHERE codigo = :codigo AND usado = 0 LIMIT 1")
    suspend fun getCodigoActivo(codigo: String): CodigoVinculacion?

    @Update
    suspend fun updateCodigoVinculacion(codigo: CodigoVinculacion)

    @Insert
    suspend fun insertVinculacion(vinculacion: Vinculacion): Long

    @Insert
    suspend fun insertMedicamento(medicamento: Medicamento): Long

    @Update
    suspend fun updateMedicamento(medicamento: Medicamento)

    @Query("SELECT * FROM medicamentos WHERE adultoMayorId = :adultoMayorId AND activo = 1 LIMIT 1")
    fun getMedicamentoActivo(adultoMayorId: Long): Flow<Medicamento?>

    @Insert
    suspend fun insertToma(toma: Toma): Long

    @Update
    suspend fun updateToma(toma: Toma)

    @Query("SELECT * FROM tomas WHERE adultoMayorId = :adultoMayorId ORDER BY horaProgramada DESC")
    fun getTomasPorAdultoMayor(adultoMayorId: Long): Flow<List<Toma>>

    @Insert
    suspend fun insertRegistroToma(registro: RegistroToma): Long

    @Query("SELECT * FROM registros_toma WHERE tomaId = :tomaId ORDER BY fechaHora ASC")
    fun getHistorialPorToma(tomaId: Long): Flow<List<RegistroToma>>

    @Insert
    suspend fun insertNotificacion(notificacion: Notificacion): Long

    @Query("SELECT * FROM notificaciones WHERE destinatarioId = :usuarioId AND entregada = 0")
    fun getNotificacionesPendientes(usuarioId: Long): Flow<List<Notificacion>>
}