package com.example.apppisc.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val password: String,
    val isAdmin: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) 