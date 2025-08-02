package com.psipro.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AnamneseModel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val isDefault: Boolean = false,
    val ownerUserId: String? = null
) 



