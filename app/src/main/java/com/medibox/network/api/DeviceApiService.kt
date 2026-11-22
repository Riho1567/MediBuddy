package com.medibox.network.api

import com.medibox.network.model.*
import retrofit2.Response
import retrofit2.http.*

interface DeviceApiService {

    @GET("api/v1/devices/{deviceId}/schedules")
    suspend fun getSchedules(
        @Path("deviceId") deviceId: String
    ): Response<List<ScheduleDto>>

    @POST("api/v1/devices/{deviceId}/schedules")
    suspend fun createSchedule(
        @Path("deviceId") deviceId: String,
        @Body req: CreateScheduleDto
    ): Response<ScheduleDto>

    @PUT("api/v1/devices/{deviceId}/schedules/{scheduleId}")
    suspend fun updateSchedule(
        @Path("deviceId") deviceId: String,
        @Path("scheduleId") scheduleId: Long,
        @Body req: UpdateScheduleDto
    ): Response<ScheduleDto>

    @DELETE("api/v1/devices/{deviceId}/schedules/{scheduleId}")
    suspend fun deleteSchedule(
        @Path("deviceId") deviceId: String,
        @Path("scheduleId") scheduleId: Long,
        @Query("clientType") clientType: String = "CUIDADOR"
    ): Response<Map<String, Any>>

    @PATCH("api/v1/devices/{deviceId}/schedules/{scheduleId}/status")
    suspend fun updateScheduleStatus(
        @Path("deviceId") deviceId: String,
        @Path("scheduleId") scheduleId: Long,
        @Body req: UpdateStatusDto
    ): Response<ScheduleDto>

    @POST("api/v1/devices/{deviceId}/command")
    suspend fun sendCommand(
        @Path("deviceId") deviceId: String,
        @Body req: CommandDto
    ): Response<CommandResponseDto>
}
