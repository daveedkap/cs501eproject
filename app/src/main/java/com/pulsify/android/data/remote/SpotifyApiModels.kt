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

data class SpotifyAudioFeature(
    @Json(name = "id") val id: String?,
    @Json(name = "tempo") val tempo: Double? = null,
)

data class SpotifyAudioFeaturesResponse(
    @Json(name = "audio_features") val audioFeatures: List<SpotifyAudioFeature?>? = null,
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

data class SpotifyDevice(
    @Json(name = "id") val id: String?,
    @Json(name = "is_active") val isActive: Boolean? = null,
    @Json(name = "name") val name: String?,
    @Json(name = "type") val type: String?,
)

data class SpotifyDevicesResponse(
    @Json(name = "devices") val devices: List<SpotifyDevice>? = null,
)

data class SpotifyPlayRequest(
    @Json(name = "uris") val uris: List<String>? = null,
    @Json(name = "offset") val offset: SpotifyPlayOffset? = null,
)

data class SpotifyPlayOffset(
    @Json(name = "position") val position: Int? = null,
)
