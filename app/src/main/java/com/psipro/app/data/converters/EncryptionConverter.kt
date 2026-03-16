package com.psipro.app.data.converters

import androidx.room.TypeConverter
import com.psipro.app.security.EncryptionManager

class EncryptionConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun encrypt(value: String?): String? {
            return value?.let { EncryptionManager.encryptStatic(it) }
        }

        @TypeConverter
        @JvmStatic
        fun decrypt(value: String?): String? {
            return value?.let { EncryptionManager.decryptStatic(it) }
        }
    }
} 



