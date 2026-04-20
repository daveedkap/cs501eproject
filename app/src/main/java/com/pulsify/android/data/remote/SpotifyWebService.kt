package com.pulsify.android.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface SpotifyWebService {

    @GET("v1/me")
    suspend fun getCurrentProfile(
        @Header("Authorization") auth: String,
    ): SpotifyUserProfile

    @GET("v1/me/player/recently-played")
    suspend fun getRecentlyPlayed(
        @Header("Authorization") auth: String,
        @Query("limit") limit: Int = 20,
    ): SpotifyRecentlyPlayedResponse

    @GET("v1/me/top/tracks")
    suspend fun getTopTracks(
        @Header("Authorization") auth: String,
        @Query("limit") limit: Int = 20,
        @Query("time_range") timeRange: String = "short_term",
    ): SpotifyTopTracksResponse

    @GET("v1/search")
    suspend fun searchTracks(
        @Header("Authorization") auth: String,
        @Query("q") query: String,
        @Query("type") type: String = "track",
        @Query("limit") limit: Int = 5,
    ): SpotifySearchResponse

    @GET("v1/me/player/devices")
    suspend fun getDevices(
        @Header("Authorization") auth: String,
    ): SpotifyDevicesResponse

    @PUT("v1/me/player/play")
    suspend fun startPlayback(
        @Header("Authorization") auth: String,
        @Query("device_id") deviceId: String? = null,
        @Body body: SpotifyPlayRequest,
    ): Response<Unit>

    @PUT("v1/me/player/pause")
    suspend fun pausePlayback(
        @Header("Authorization") auth: String,
        @Query("device_id") deviceId: String? = null,
    ): Response<Unit>

    @POST("v1/me/player/next")
    suspend fun skipToNext(
        @Header("Authorization") auth: String,
        @Query("device_id") deviceId: String? = null,
    ): Response<Unit>

    @POST("v1/me/player/previous")
    suspend fun skipToPrevious(
        @Header("Authorization") auth: String,
        @Query("device_id") deviceId: String? = null,
    ): Response<Unit>
}
