package com.medibox.network

import android.content.Context
import com.medibox.network.api.AuthApiService
import com.medibox.network.api.DeviceApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var appContext: Context? = null
    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private fun getOkHttpClient(context: Context): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val sessionManager = SessionManager(context)
            val token = sessionManager.getToken()

            val requestBuilder = chain.request().newBuilder()
            if (!token.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
                requestBuilder.addHeader("X-Device-Id", sessionManager.getDeviceId())
            }
            chain.proceed(requestBuilder.build())
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private fun getRetrofit(context: Context): Retrofit {
        val baseUrl = ApiConfig.getBaseUrl(context)
        if (retrofit == null || currentBaseUrl != baseUrl) {
            currentBaseUrl = baseUrl
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(getOkHttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun getAuthApi(context: Context): AuthApiService {
        return getRetrofit(context).create(AuthApiService::class.java)
    }

    fun getDeviceApi(context: Context): DeviceApiService {
        return getRetrofit(context).create(DeviceApiService::class.java)
    }
}
