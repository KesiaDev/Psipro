package com.psipro.app.sync.api

import com.google.gson.annotations.SerializedName

data class BackendLoginRequest(
    val email: String,
    val password: String
)

data class BackendLoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    val user: BackendUserBasic
)

data class BackendUserBasic(
    val id: String,
    val email: String,
    val name: String? = null,
    val fullName: String? = null
)

data class BackendMeResponse(
    val id: String,
    val email: String,
    val role: String,
    val clinicId: String?,
    val name: String?
)

data class SyncPatientsRequest(
    val patients: List<SyncPatientPayload>
)

data class SyncPatientPayload(
    val id: String? = null,
    val clinicId: String,
    val name: String,
    val birthDate: String? = null,
    val cpf: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val emergencyContact: String? = null,
    val observations: String? = null,
    val status: String? = null,
    val type: String? = null,
    val sharedWith: List<String>? = null,
    val origin: String? = "ANDROID",
    val source: String? = "app",
    val updatedAt: String
)

/**
 * Modelo retornado pelo backend (Prisma patient).
 * Campos extras são ignorados pelo Gson.
 */
data class RemotePatient(
    val id: String,
    val clinicId: String,
    val name: String,
    val birthDate: String? = null,
    val cpf: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val emergencyContact: String? = null,
    val observations: String? = null,
    val status: String? = null,
    val type: String? = null,
    val sharedWith: List<String>? = null,
    val source: String? = null,
    val origin: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

