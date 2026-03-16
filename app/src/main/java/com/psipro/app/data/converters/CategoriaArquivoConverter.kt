package com.psipro.app.data.converters

import androidx.room.TypeConverter
import com.psipro.app.data.entities.CategoriaArquivo

class CategoriaArquivoConverter {
    @TypeConverter
    fun fromCategoriaArquivo(categoria: CategoriaArquivo?): String? {
        return categoria?.name
    }

    @TypeConverter
    fun toCategoriaArquivo(categoria: String?): CategoriaArquivo? {
        return categoria?.let { CategoriaArquivo.valueOf(it) }
    }
} 



