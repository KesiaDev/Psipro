package com.psipro.app.data.repository

import com.psipro.app.data.dao.VidaEmocionalDao
import com.psipro.app.data.entities.VidaEmocional
import javax.inject.Inject

class VidaEmocionalRepository @Inject constructor(
    private val dao: VidaEmocionalDao
) {
    suspend fun getByPatientId(patientId: Long): VidaEmocional? = dao.getByPatientId(patientId)
    suspend fun insert(vidaEmocional: VidaEmocional): Long = dao.insert(vidaEmocional)
    suspend fun update(vidaEmocional: VidaEmocional) = dao.update(vidaEmocional)
} 



