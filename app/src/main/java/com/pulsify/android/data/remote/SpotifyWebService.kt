package com.pulsify.android.data.remote

import retrofit2.http.GET

/**
 * Skeleton Spotify Web API. Not authenticated or called in this build; playback is simulated in-app.
 */
interface SpotifyWebService {
    @GET("v1/me/player")
    suspend fun getCurrentPlayback(): SpotifyPlaybackStateDto
}
