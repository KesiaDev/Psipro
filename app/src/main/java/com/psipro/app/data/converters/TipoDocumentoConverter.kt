package com.psipro.app.data.converters

import androidx.room.TypeConverter
import com.psipro.app.data.entities.TipoDocumento

class TipoDocumentoConverter {
    
    @TypeConverter
    fun fromTipoDocumento(tipo: TipoDocumento): String {
        return tipo.name
    }
    
    @TypeConverter
    fun toTipoDocumento(tipo: String): TipoDocumento {
        return try {
            TipoDocumento.valueOf(tipo)
        } catch (e: IllegalArgumentException) {
            TipoDocumento.DOCUMENTO_PERSONALIZADO
        }
    }
} 



