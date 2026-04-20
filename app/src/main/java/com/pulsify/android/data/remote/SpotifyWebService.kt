package com.pulsify.android.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
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
}
