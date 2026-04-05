package com.pulsify.android.data.repository

import com.pulsify.android.BuildConfig
import com.pulsify.android.data.local.ActivitySessionEntity
import com.pulsify.android.data.local.ContextMusicRuleEntity
import com.pulsify.android.data.local.PulsifyDao
import com.pulsify.android.data.remote.GeminiContent
import com.pulsify.android.data.remote.GeminiContentPart
import com.pulsify.android.data.remote.GeminiGenerateRequest
import com.pulsify.android.data.remote.GeminiPlaylistService
import com.pulsify.android.domain.ChatMessage
import com.pulsify.android.domain.DetectedActivity
import com.pulsify.android.domain.PlaybackUiState
import com.pulsify.android.domain.Track
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers

class PulsifyRepository(
    private val dao: PulsifyDao,
    private val geminiPlaylistService: GeminiPlaylistService,
) {
    val sessions = dao.observeSessions()

    val contextRules = dao.observeRules()

    private val _messages = MutableStateFlow<List<ChatMessage>>(defaultWelcome())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _playback = MutableStateFlow(
        PlaybackUiState(isPlaying = false, currentIndex = 0, tracks = emptyList()),
    )
    val playback: StateFlow<PlaybackUiState> = _playback.asStateFlow()

    private val _spotifyLinked = MutableStateFlow(false)
    val spotifyLinked: StateFlow<Boolean> = _spotifyLinked.asStateFlow()

    private val _textModePreferred = MutableStateFlow(false)
    val textModePreferred = _textModePreferred.asStateFlow()

    fun setSpotifyLinked(linked: Boolean) {
        _spotifyLinked.value = linked
    }

    fun setTextModePreferred(enabled: Boolean) {
        _textModePreferred.value = enabled
    }

    fun appendMessage(text: String, isUser: Boolean) {
        val next = _messages.value + ChatMessage(
            id = Random.nextLong().toString(),
            isUser = isUser,
            text = text,
        )
        _messages.value = next
    }

    fun clearConversation() {
        _messages.value = defaultWelcome()
    }

    /**
     * Simulates network + model latency, optionally exercises Retrofit types when mocks are disabled.
     */
    suspend fun requestPlaylistForActivity(
        activity: DetectedActivity,
        userPrompt: String?,
        latitude: Double?,
        longitude: Double?,
    ): Long = withContext(Dispatchers.IO) {
        if (!BuildConfig.USE_MOCK_APIS) {
            runCatching {
                geminiPlaylistService.generatePlaylistSuggestion(
                    apiKey = "",
                    body = GeminiGenerateRequest(
                        contents = listOf(
                            GeminiContent(
                                parts = listOf(
                                    GeminiContentPart(
                                        text = buildPrompt(activity, userPrompt, latitude, longitude),
                                    ),
                                ),
                                role = "user",
                            ),
                        ),
                    ),
                )
            }
        }

        delay(900L)
        val tracks = mockTracksFor(activity)
        _playback.value = PlaybackUiState(
            isPlaying = false,
            currentIndex = 0,
            tracks = tracks,
        )

        val summary = "${tracks.size} tracks · ${activity.displayLabel()}"
        val sessionId = dao.insertSession(
            ActivitySessionEntity(
                timestampMillis = System.currentTimeMillis(),
                activityType = activity.name,
                latitude = latitude,
                longitude = longitude,
                playlistSummary = summary,
                trackCount = tracks.size,
            ),
        )

        dao.upsertRule(
            ContextMusicRuleEntity(
                activityType = activity.name,
                associationNote = userPrompt?.take(120) ?: "Default contextual mix",
                useCount = 1,
            ),
        )

        appendMessage(
            "Here is a ${tracks.size}-track mix tuned for ${activity.displayLabel().lowercase()}. " +
                "Playback is simulated until Spotify is linked.",
            isUser = false,
        )

        sessionId
    }

    fun togglePlayPause() {
        val p = _playback.value
        if (p.tracks.isEmpty()) return
        _playback.value = p.copy(isPlaying = !p.isPlaying)
    }

    fun skipNext() {
        val p = _playback.value
        if (p.tracks.isEmpty()) return
        val next = (p.currentIndex + 1).coerceAtMost(p.tracks.lastIndex)
        _playback.value = p.copy(currentIndex = next, isPlaying = true)
    }

    fun skipPrevious() {
        val p = _playback.value
        if (p.tracks.isEmpty()) return
        val prev = (p.currentIndex - 1).coerceAtLeast(0)
        _playback.value = p.copy(currentIndex = prev, isPlaying = true)
    }

    private fun defaultWelcome(): List<ChatMessage> = listOf(
        ChatMessage(
            id = "welcome",
            isUser = false,
            text = "Hi, I’m Pulsify. I’ll read your movement context and suggest music that fits. " +
                "Spotify and cloud AI calls are stubbed for now—everything you see is local simulation.",
        ),
    )

    private fun buildPrompt(
        activity: DetectedActivity,
        userPrompt: String?,
        lat: Double?,
        lng: Double?,
    ): String = buildString {
        append("Activity: ${activity.name}. ")
        userPrompt?.let { append("User note: $it. ") }
        if (lat != null && lng != null) append("Approx location: $lat,$lng. ")
        append("Return a 10-song playlist idea with tempo and energy matched to the activity.")
    }

    private fun mockTracksFor(activity: DetectedActivity): List<Track> {
        val pool = when (activity) {
            DetectedActivity.Running -> RUN_LIST
            DetectedActivity.Walking -> WALK_LIST
            DetectedActivity.Sitting -> STUDY_LIST
            DetectedActivity.Unknown -> WALK_LIST
        }
        return pool.shuffled().take(10)
    }

    companion object {
        private val RUN_LIST = listOf(
            Track("1", "Pulse Driver", "Nova Run", 168, "High energy"),
            Track("2", "Sprint Theory", "Metro Fit", 172, "Peak BPM"),
            Track("3", "Redline", "Kinetic", 165, "Driving"),
            Track("4", "Heartbeat City", "Lane 8 Run Club", 160, "Steady"),
            Track("5", "Tempo Trap", "DJ Stride", 170, "Aggressive"),
            Track("6", "Cadence", "Athletica", 158, "Rhythmic"),
            Track("7", "Oxygen", "Skyline", 166, "Airy"),
            Track("8", "Power Curve", "Ironwave", 174, "Intense"),
            Track("9", "Afterburn", "Neon Pace", 169, "Punchy"),
            Track("10", "Finish Line", "Echo Track", 162, "Triumphant"),
            Track("11", "Intervals", "Coach Mode", 171, "Percussive"),
            Track("12", "Uphill", "Summit", 164, "Motivational"),
        )

        private val WALK_LIST = listOf(
            Track("w1", "Sidewalk Stories", "Indie Atlas", 108, "Mid-tempo"),
            Track("w2", "Golden Hour Walk", "Sunset Kids", 102, "Warm"),
            Track("w3", "Campus Loop", "Paper Planes", 98, "Light"),
            Track("w4", "Coffee Run", "Latte Sky", 110, "Easy"),
            Track("w5", "River Path", "Blue Fern", 105, "Flowing"),
            Track("w6", "Transit", "Metro Lines", 112, "Groove"),
            Track("w7", "Soft Steps", "Quiet Type", 96, "Mellow"),
            Track("w8", "Neighborhood", "Porch Lights", 100, "Friendly"),
            Track("w9", "Breeze", "Open Air", 104, "Breathy"),
            Track("w10", "Crosswalk", "City Bloom", 106, "Indie"),
            Track("w11", "Park Bench", "Slow Sunday", 94, "Relaxed"),
            Track("w12", "Detour", "Atlas Jr.", 109, "Curious"),
        )

        private val STUDY_LIST = listOf(
            Track("s1", "Deep Focus", "Loft Study", 82, "Minimal"),
            Track("s2", "Pencil Sketches", "Quiet Hours", 78, "Soft"),
            Track("s3", "Library Lights", "Instrumental Minds", 80, "Calm"),
            Track("s4", "Night Notes", "Tape Deck", 76, "Lo-fi"),
            Track("s5", "Glass Desk", "Ambient Lab", 74, "Air"),
            Track("s6", "Equation", "Math Rain", 84, "Steady"),
            Track("s7", "Highlighter", "Pastel Beat", 79, "Gentle"),
            Track("s8", "Caffeine Low", "Muted Drum", 83, "Subtle"),
            Track("s9", "Chapter 4", "Reader", 77, "Warm pads"),
            Track("s10", "Exam Mode", "Silence +1", 81, "Focused"),
            Track("s11", "Index Cards", "Study Hall", 75, "Light pulse"),
            Track("s12", "Deadline Soft", "Cloud Notes", 86, "Neutral"),
        )
    }
}
