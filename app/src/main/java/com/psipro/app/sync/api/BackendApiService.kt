package com.psipro.app.sync.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface BackendApiService {
    @POST("auth/login")
    suspend fun login(@Body body: BackendLoginRequest): Response<BackendLoginResponse>

    @POST("auth/register")
    suspend fun register(@Body body: BackendRegisterRequest): Response<BackendLoginResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: BackendRefreshRequest): Response<BackendRefreshResponse>

    @POST("auth/logout")
    suspend fun logout(@Body body: BackendLogoutRequest): Response<Unit>

    @POST("auth/switch-clinic")
    suspend fun switchClinic(@Body body: BackendSwitchClinicRequest): Response<BackendSwitchClinicResponse>

    @GET("auth/me")
    suspend fun me(): Response<BackendMeResponse>

    @GET("clinics")
    suspend fun getClinics(): Response<List<RemoteClinic>>

    @POST("sync/patients")
    suspend fun syncPatients(
        @Query("clinicId") clinicId: String,
        @Body body: SyncPatientsRequest
    ): Response<List<RemotePatient>>

    @GET("sync/patients")
    suspend fun getPatients(
        @Query("clinicId") clinicId: String,
        @Query("updatedAfter") updatedAfter: String? = null
    ): Response<List<RemotePatient>>

    @GET("sync/appointments")
    suspend fun getAppointments(
        @Query("clinicId") clinicId: String,
        @Query("updatedAfter") updatedAfter: String? = null
    ): Response<List<RemoteAppointment>>

    @POST("sync/appointments")
    suspend fun syncAppointments(
        @Query("clinicId") clinicId: String,
        @Body body: SyncAppointmentsRequest
    ): Response<List<RemoteAppointment>>

    @GET("sync/sessions")
    suspend fun getSessions(
        @Query("clinicId") clinicId: String,
        @Query("updatedAfter") updatedAfter: String? = null
    ): Response<List<RemoteSession>>

    @POST("sync/sessions")
    suspend fun syncSessions(
        @Query("clinicId") clinicId: String,
        @Body body: SyncSessionsRequest
    ): Response<List<RemoteSession>>

    @GET("sync/payments")
    suspend fun getPayments(
        @Query("clinicId") clinicId: String,
        @Query("updatedAfter") updatedAfter: String? = null
    ): Response<List<RemotePayment>>

    @POST("sync/payments")
    suspend fun syncPayments(
        @Query("clinicId") clinicId: String,
        @Body body: SyncPaymentsRequest
    ): Response<List<RemotePayment>>

    @GET("sync/documents")
    suspend fun getDocuments(
        @Query("clinicId") clinicId: String,
        @Query("patientId") patientId: String? = null,
        @Query("updatedAfter") updatedAfter: String? = null
    ): Response<List<RemoteDocument>>

    @POST("sync/documents")
    suspend fun syncDocuments(
        @Query("clinicId") clinicId: String,
        @Body body: SyncDocumentsRequest
    ): Response<List<RemoteDocument>>

    @Multipart
    @POST("voice/transcribe")
    suspend fun transcribe(@Part file: MultipartBody.Part): Response<TranscribeResponse>

    @POST("sessions")
    suspend fun createSession(@Body body: CreateSessionRequest): Response<SessionResponse>

    @POST("sessions/voice-note")
    suspend fun voiceNote(@Body body: VoiceNoteRequest): Response<SessionResponse>
}

