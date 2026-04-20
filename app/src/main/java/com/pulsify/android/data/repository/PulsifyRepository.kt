package com.pulsify.android.data.repository

import com.pulsify.android.BuildConfig
import com.pulsify.android.data.local.ActivitySessionEntity
import com.pulsify.android.data.local.ContextMusicRuleEntity
import com.pulsify.android.data.local.PulsifyDao
import com.pulsify.android.data.remote.GeminiContent
import com.pulsify.android.data.remote.GeminiContentPart
import com.pulsify.android.data.remote.GeminiGenerateRequest
import com.pulsify.android.data.remote.GeminiPlaylistService
import com.pulsify.android.data.remote.SpotifyAuthManager
import com.pulsify.android.data.remote.SpotifyTrackObject
import com.pulsify.android.data.remote.SpotifyWebService
import com.pulsify.android.domain.ChatMessage
import com.pulsify.android.domain.DetectedActivity
import com.pulsify.android.domain.PlaybackUiState
import com.pulsify.android.domain.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.random.Random

class PulsifyRepository(
    private val dao: PulsifyDao,
    private val geminiPlaylistService: GeminiPlaylistService,
    private val spotifyWebService: SpotifyWebService,
    private val spotifyAuthManager: SpotifyAuthManager,
) {
    val sessions = dao.observeSessions()
    val contextRules = dao.observeRules()

    private val _messages = MutableStateFlow<List<ChatMessage>>(defaultWelcome())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _playback = MutableStateFlow(
        PlaybackUiState(isPlaying = false, currentIndex = 0, tracks = emptyList()),
    )
    val playback: StateFlow<PlaybackUiState> = _playback.asStateFlow()

    val spotifyLinked: StateFlow<Boolean> = spotifyAuthManager.isAuthenticated

    private val _textModePreferred = MutableStateFlow(false)
    val textModePreferred = _textModePreferred.asStateFlow()

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

    suspend fun requestPlaylistForActivity(
        activity: DetectedActivity,
        userPrompt: String?,
        latitude: Double?,
        longitude: Double?,
    ): Long = withContext(Dispatchers.IO) {
        val spotifyTracks = fetchSpotifyTracks()
        val tracks: List<Track>

        if (spotifyTracks.isNotEmpty()) {
            tracks = spotifyTracks.take(10).mapIndexed { i, st ->
                st.toDomainTrack(activity, i)
            }
            tryGeminiReasoning(activity, userPrompt, spotifyTracks)
        } else {
            tracks = mockTracksFor(activity)
            tryGeminiGeneric(activity, userPrompt, latitude, longitude)
        }

        _playback.value = PlaybackUiState(
            isPlaying = false,
            currentIndex = 0,
            tracks = tracks,
        )

        val avgBpm = tracks.map { it.bpm }.average().toInt()
        val moods = tracks.map { it.energyLabel }.distinct().take(3).joinToString(", ")
        val summary = "${tracks.size} tracks · ${activity.displayLabel()} · ~${avgBpm} BPM avg · $moods"

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
                associationNote = buildAssociationLabel(activity, tracks),
                useCount = 1,
            ),
        )

        if (spotifyTracks.isEmpty()) {
            appendMessage(
                "Here's a ${tracks.size}-track mix tuned for ${activity.displayLabel().lowercase()}. " +
                    "Connect Spotify in Settings for real music from your library!",
                isUser = false,
            )
        }

        sessionId
    }

    private suspend fun fetchSpotifyTracks(): List<SpotifyTrackObject> {
        if (!spotifyAuthManager.isAuthenticated.value) return emptyList()
        val token = spotifyAuthManager.getValidAccessToken() ?: return emptyList()
        val bearer = "Bearer $token"

        val topTracks = runCatching {
            spotifyWebService.getTopTracks(bearer, limit = 20, timeRange = "short_term")
        }.getOrNull()?.items

        if (!topTracks.isNullOrEmpty()) return topTracks

        val recent = runCatching {
            spotifyWebService.getRecentlyPlayed(bearer, limit = 20)
        }.getOrNull()?.items?.map { it.track }

        return recent?.distinctBy { it.id } ?: emptyList()
    }

    suspend fun fetchUserProfileName() {
        val token = spotifyAuthManager.getValidAccessToken() ?: return
        val profile = runCatching {
            spotifyWebService.getCurrentProfile("Bearer $token")
        }.getOrNull()
        profile?.displayName?.let { spotifyAuthManager.updateDisplayName(it) }
    }

    private suspend fun tryGeminiReasoning(
        activity: DetectedActivity,
        userPrompt: String?,
        spotifyTracks: List<SpotifyTrackObject>,
    ) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            appendMessage(
                "Built a ${spotifyTracks.size.coerceAtMost(10)}-track mix from your Spotify library for ${activity.displayLabel().lowercase()}.",
                isUser = false,
            )
            return
        }

        val trackList = spotifyTracks.take(10).joinToString("\n") { t ->
            "- ${t.name} by ${t.artists?.firstOrNull()?.name ?: "Unknown"}"
        }

        val prompt = buildString {
            append("You are Pulsify, a friendly music companion app. ")
            append("The user is currently ${activity.displayLabel().lowercase()}. ")
            userPrompt?.let { append("They said: \"$it\". ") }
            append("Their recent top tracks from Spotify:\n$trackList\n\n")
            append("In 2-3 brief, conversational sentences, explain how this music fits ")
            append("their current ${activity.displayLabel().lowercase()} context. ")
            append("Be upbeat and natural.")
        }

        val result = runCatching {
            geminiPlaylistService.generatePlaylistSuggestion(
                apiKey = apiKey,
                body = GeminiGenerateRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiContentPart(text = prompt)),
                            role = "user",
                        ),
                    ),
                ),
            )
        }.getOrNull()

        val geminiText = result?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        if (!geminiText.isNullOrBlank()) {
            appendMessage(geminiText.trim(), isUser = false)
        } else {
            appendMessage(
                "Here's a mix from your Spotify library, matched to your ${activity.displayLabel().lowercase()} context!",
                isUser = false,
            )
        }
    }

    private suspend fun tryGeminiGeneric(
        activity: DetectedActivity,
        userPrompt: String?,
        latitude: Double?,
        longitude: Double?,
    ) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) return

        val prompt = buildString {
            append("You are Pulsify, a friendly music companion app. ")
            append("The user is currently ${activity.displayLabel().lowercase()}. ")
            userPrompt?.let { append("They said: \"$it\". ") }
            if (latitude != null && longitude != null) {
                append("Approximate location: $latitude, $longitude. ")
            }
            append("In 2-3 brief, conversational sentences, suggest what kind of music ")
            append("would fit their current activity. Be specific about genres or vibes. ")
            append("Be upbeat and natural.")
        }

        val result = runCatching {
            geminiPlaylistService.generatePlaylistSuggestion(
                apiKey = apiKey,
                body = GeminiGenerateRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiContentPart(text = prompt)),
                            role = "user",
                        ),
                    ),
                ),
            )
        }.getOrNull()

        val geminiText = result?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        if (!geminiText.isNullOrBlank()) {
            appendMessage(geminiText.trim(), isUser = false)
        }
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

    private fun SpotifyTrackObject.toDomainTrack(activity: DetectedActivity, index: Int): Track {
        val estimatedBpm = when (activity) {
            DetectedActivity.Running -> 155 + (index % 20)
            DetectedActivity.Walking -> 95 + (index % 20)
            DetectedActivity.Sitting -> 70 + (index % 15)
            DetectedActivity.Unknown -> 100 + (index % 20)
        }
        val energy = when (activity) {
            DetectedActivity.Running -> "High energy"
            DetectedActivity.Walking -> "Mid-tempo"
            DetectedActivity.Sitting -> "Calm"
            DetectedActivity.Unknown -> "Balanced"
        }
        return Track(
            id = id,
            title = name,
            artist = artists?.firstOrNull()?.name ?: "Unknown",
            bpm = estimatedBpm,
            energyLabel = energy,
        )
    }

    private fun defaultWelcome(): List<ChatMessage> = listOf(
        ChatMessage(
            id = "welcome",
            isUser = false,
            text = "Hi, I'm Pulsify! I read your movement context and suggest music that fits. " +
                "Connect Spotify in Settings to use your real library, or try a mock mix right away.",
        ),
    )

    private fun buildAssociationLabel(activity: DetectedActivity, tracks: List<Track>): String {
        val avgBpm = tracks.map { it.bpm }.average().toInt()
        val topMoods = tracks.map { it.energyLabel }.distinct().shuffled().take(2).joinToString(" / ")
        return when (activity) {
            DetectedActivity.Running -> "High-energy, $topMoods — avg $avgBpm BPM for runs"
            DetectedActivity.Walking -> "Mid-tempo, $topMoods — avg $avgBpm BPM for walks"
            DetectedActivity.Sitting -> "Lo-fi & calm, $topMoods — avg $avgBpm BPM for focus"
            DetectedActivity.Unknown -> "Mixed mood, $topMoods — avg $avgBpm BPM"
        }
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
