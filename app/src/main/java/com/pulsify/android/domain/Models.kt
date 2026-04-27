package com.pulsify.android.domain

enum class DetectedActivity {
    Sitting,
    Walking,
    Running,
    Unknown;

    fun displayLabel(): String = when (this) {
        Sitting -> "Sitting"
        Walking -> "Walking"
        Running -> "Running / exercising"
        Unknown -> "Detecting…"
    }
}

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
)

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val bpm: Int?,
    val energyLabel: String,
    val spotifyUri: String? = null,
)

data class PlaybackUiState(
    val isPlaying: Boolean,
    val currentIndex: Int,
    val tracks: List<Track>,
) {
    val currentTrack: Track?
        get() = tracks.getOrNull(currentIndex)
}
