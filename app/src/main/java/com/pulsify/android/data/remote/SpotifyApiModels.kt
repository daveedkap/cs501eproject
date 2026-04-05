package com.pulsify.android.data.remote

import com.squareup.moshi.Json

data class SpotifyTrackItem(
    @Json(name = "name") val name: String?,
    @Json(name = "artists") val artists: List<SpotifyArtist>? = null,
)

data class SpotifyArtist(
    @Json(name = "name") val name: String?,
)

data class SpotifyPlaybackStateDto(
    @Json(name = "item") val item: SpotifyTrackItem? = null,
    @Json(name = "is_playing") val isPlaying: Boolean? = null,
)
