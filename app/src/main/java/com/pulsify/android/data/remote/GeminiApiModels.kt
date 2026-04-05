package com.pulsify.android.data.remote

import com.squareup.moshi.Json

/**
 * Request/response shapes for a future Gemini integration. Retrofit is wired; calls are not executed
 * while [com.pulsify.android.BuildConfig.USE_MOCK_APIS] is true.
 */
data class GeminiContentPart(
    @Json(name = "text") val text: String,
)

data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiContentPart>,
    @Json(name = "role") val role: String? = null,
)

data class GeminiGenerateRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
)

data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null,
)

data class GeminiGenerateResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null,
)
