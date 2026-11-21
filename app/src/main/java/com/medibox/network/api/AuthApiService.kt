package com.medibox.network.api

import com.medibox.network.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/auth/register")
    suspend fun register(@Body req: RegisterDto): Response<AuthResponseDto>

    @POST("api/v1/auth/login")
    suspend fun login(@Body req: LoginDto): Response<AuthResponseDto>

    @POST("api/v1/auth/pairing/generate")
    suspend fun generatePairingCode(@Body req: GenerateCodeDto): Response<PairingCodeResponseDto>

    @POST("api/v1/auth/pairing/link")
    suspend fun linkWithCode(@Body req: LinkCodeDto): Response<LinkResponseDto>
}
