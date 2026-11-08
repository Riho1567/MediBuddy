package com.medibox.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.medibox.data.datos.MediBoxDao
import com.medibox.data.modelo.CodigoVinculacion
import com.medibox.data.modelo.Converters
import com.medibox.data.modelo.Medicamento
import com.medibox.data.modelo.Notificacion
import com.medibox.data.modelo.RegistroToma
import com.medibox.data.modelo.Toma
import com.medibox.data.modelo.Usuario
import com.medibox.data.modelo.Vinculacion

@Database(
    entities = [
        Usuario::class,
        CodigoVinculacion::class,
        Vinculacion::class,
        Medicamento::class,
        Toma::class,
        RegistroToma::class,
        Notificacion::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MediBoxDatabase : RoomDatabase() {

    abstract fun mediBoxDao(): MediBoxDao

    companion object {
        @Volatile
        private var INSTANCE: MediBoxDatabase? = null

        fun getInstance(context: Context): MediBoxDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MediBoxDatabase::class.java,
                    "medibox_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}