package com.psipro.app.sync.api

import com.google.gson.annotations.SerializedName

data class BackendLoginRequest(
    val email: String,
    val password: String
)

data class BackendLoginResponse(
    @SerializedName("accessToken")
    val accessToken: String? = null,
    @SerializedName("access_token")
    val accessTokenLegacy: String? = null,
    val refreshToken: String? = null,
    val user: BackendUserBasic
) {
    fun effectiveAccessToken(): String = accessToken ?: accessTokenLegacy ?: ""
}

data class BackendRegisterRequest(
    val email: String,
    val password: String,
    val fullName: String
)

data class BackendRefreshRequest(
    val refreshToken: String
)

data class BackendRefreshResponse(
    @SerializedName("accessToken")
    val accessToken: String? = null,
    @SerializedName("access_token")
    val accessTokenLegacy: String? = null,
    val refreshToken: String? = null
) {
    fun effectiveAccessToken(): String = accessToken ?: accessTokenLegacy ?: ""
}

data class BackendLogoutRequest(
    val refreshToken: String
)

data class BackendSwitchClinicRequest(
    val clinicId: String
)

data class BackendSwitchClinicResponse(
    @SerializedName("accessToken")
    val accessToken: String? = null,
    val clinicId: String? = null
) {
    fun effectiveAccessToken(): String = accessToken ?: ""
}

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

data class SyncAppointmentsRequest(
    val appointments: List<SyncAppointmentPayload>
)

data class SyncAppointmentPayload(
    val id: String,
    val patientId: String,
    val professionalId: String,
    val scheduledAt: String,
    val duration: Int,
    val updatedAt: String,
    val type: String? = null,
    val notes: String? = null,
    val status: String? = null
)

data class RemoteAppointment(
    val id: String,
    val clinicId: String? = null,
    val patientId: String,
    val professionalId: String,
    val scheduledAt: String,
    val duration: Int,
    val type: String? = null,
    val notes: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class SyncSessionsRequest(val sessions: List<SyncSessionPayload>)
data class SyncSessionPayload(
    val id: String,
    val patientId: String,
    val professionalId: String,
    val date: String,
    val duration: Int,
    val updatedAt: String,
    val notes: String? = null,
    val status: String? = null
)
data class RemoteSession(
    val id: String,
    val patientId: String,
    val professionalId: String,
    val date: String,
    val duration: Int,
    val notes: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class SyncPaymentsRequest(val payments: List<SyncPaymentPayload>)
data class SyncPaymentPayload(
    val id: String,
    val sessionId: String,
    val amount: Double,
    val updatedAt: String,
    val status: String? = null,
    val paidAt: String? = null
)
data class RemotePayment(
    val id: String,
    val sessionId: String? = null,
    val amount: Double,
    val status: String? = null,
    val paidAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

