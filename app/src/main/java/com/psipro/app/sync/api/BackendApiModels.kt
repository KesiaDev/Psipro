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
    val fullName: String,
    val professionalType: String? = null
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
    val fullName: String? = null,
    val professionalType: String? = null,
    val clinicId: String? = null
)

data class RemoteClinic(
    val id: String,
    val name: String,
    val status: String? = null,
    val plan: String? = null
)

data class BackendConsentResponse(
    val success: Boolean? = null,
    val message: String? = null
)

data class BackendMeResponse(
    val id: String,
    val email: String,
    val role: String,
    val clinicId: String?,
    val name: String?,
    val professionalType: String? = null,
    val lgpdAcceptedAt: String? = null
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

/** Paciente incluído no agendamento para criar localmente se não existir */
data class RemoteAppointmentPatient(
    val id: String,
    val name: String,
    val phone: String? = null
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
    val updatedAt: String? = null,
    /** Incluído pelo backend para permitir criar paciente local ao puxar agendamento */
    val patient: RemoteAppointmentPatient? = null
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

data class SyncDocumentsRequest(val documents: List<SyncDocumentPayload>)
data class SyncDocumentPayload(
    val id: String? = null,
    val patientId: String,
    val name: String,
    val type: String,
    val fileUrl: String? = null,
    val content: Map<String, Any?>? = null,
    val updatedAt: String,
    val source: String? = "app",
    val status: String? = "Ativo"
)
data class RemoteDocument(
    val id: String,
    val userId: String? = null,
    val patientId: String? = null,
    val name: String,
    val type: String,
    val fileUrl: String? = null,
    val content: Map<String, Any?>? = null,
    val status: String? = null,
    val source: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class TranscribeResponse(
    val transcript: String
)

/** Requisição para criar sessão no backend */
data class CreateSessionRequest(
    val patientId: String,
    val date: String,
    val duration: Int = 60,
    val status: String = "realizada",
    val notes: String? = null,
    val source: String = "app"
)

/** Sessão retornada pelo backend (create ou voice-note) */
data class SessionResponse(
    val id: String,
    val patientId: String,
    val date: String,
    val notes: String? = null,
    val transcript: String? = null,
    val summary: String? = null,
    val themes: List<String>? = null,
    val emotions: List<String>? = null,
    val actionItems: List<String>? = null,
    val riskFlags: List<String>? = null
)

/** Payload para voice-note (transcript + insights) */
data class VoiceNoteRequest(
    val sessionId: String,
    val transcript: String
)

