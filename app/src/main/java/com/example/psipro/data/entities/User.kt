package com.example.psipro.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val password: String,
    val name: String = "",
    val crp: String = "",
    val photoPath: String? = null,
    val isAdmin: Boolean = false
)









