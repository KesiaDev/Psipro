package com.example.psipro.data.repository

import com.example.psipro.data.dao.VidaEmocionalDao
import com.example.psipro.data.entities.VidaEmocional
import javax.inject.Inject

class VidaEmocionalRepository @Inject constructor(
    private val dao: VidaEmocionalDao
) {
    suspend fun getByPatientId(patientId: Long): VidaEmocional? = dao.getByPatientId(patientId)
    suspend fun insert(vidaEmocional: VidaEmocional): Long = dao.insert(vidaEmocional)
    suspend fun update(vidaEmocional: VidaEmocional) = dao.update(vidaEmocional)
} 