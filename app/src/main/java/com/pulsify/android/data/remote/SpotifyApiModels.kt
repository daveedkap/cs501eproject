package com.pulsify.android.data.remote

import com.squareup.moshi.Json

data class SpotifyArtist(
    @Json(name = "name") val name: String?,
)

data class SpotifyImage(
    @Json(name = "url") val url: String?,
)

data class SpotifyAlbum(
    @Json(name = "name") val name: String?,
    @Json(name = "images") val images: List<SpotifyImage>? = null,
)

data class SpotifyTrackObject(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "artists") val artists: List<SpotifyArtist>? = null,
    @Json(name = "album") val album: SpotifyAlbum? = null,
    @Json(name = "duration_ms") val durationMs: Int? = null,
    @Json(name = "uri") val uri: String? = null,
)

data class SpotifyUserProfile(
    @Json(name = "id") val id: String?,
    @Json(name = "display_name") val displayName: String?,
    @Json(name = "images") val images: List<SpotifyImage>? = null,
)

data class SpotifyPlayHistoryObject(
    @Json(name = "track") val track: SpotifyTrackObject,
    @Json(name = "played_at") val playedAt: String? = null,
)

data class SpotifyRecentlyPlayedResponse(
    @Json(name = "items") val items: List<SpotifyPlayHistoryObject>? = null,
)

data class SpotifyTopTracksResponse(
    @Json(name = "items") val items: List<SpotifyTrackObject>? = null,
)

data class SpotifySearchTracksWrapper(
    @Json(name = "items") val items: List<SpotifyTrackObject>? = null,
)

data class SpotifySearchResponse(
    @Json(name = "tracks") val tracks: SpotifySearchTracksWrapper? = null,
)
