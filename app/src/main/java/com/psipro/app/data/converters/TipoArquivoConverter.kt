package com.psipro.app.data.converters

import androidx.room.TypeConverter
import com.psipro.app.data.entities.TipoArquivo

class TipoArquivoConverter {
    @TypeConverter
    fun fromTipoArquivo(tipo: TipoArquivo?): String? {
        return tipo?.name
    }

    @TypeConverter
    fun toTipoArquivo(tipo: String?): TipoArquivo? {
        return tipo?.let { TipoArquivo.valueOf(it) }
    }
} 



