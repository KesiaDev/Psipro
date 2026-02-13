package com.psipro.app.sync.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.psipro.app.BuildConfig
import com.psipro.app.sync.BackendSessionStore
import com.psipro.app.sync.api.AuthInterceptor
import com.psipro.app.sync.api.BackendApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncNetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionStore: BackendSessionStore): AuthInterceptor {
        return AuthInterceptor(sessionStore)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, client: OkHttpClient): Retrofit {
        // Garantir barra no final para Retrofit
        val base = BuildConfig.PSIPRO_API_BASE_URL.let { if (it.endsWith("/")) it else "$it/" }
        return Retrofit.Builder()
            .baseUrl(base)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideBackendApi(retrofit: Retrofit): BackendApiService {
        return retrofit.create(BackendApiService::class.java)
    }
}

