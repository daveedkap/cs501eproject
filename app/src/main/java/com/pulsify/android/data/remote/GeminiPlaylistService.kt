package com.pulsify.android.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Skeleton Retrofit service for Gemini playlist reasoning. Base URL and API key are not configured
 * for production use in this milestone.
 */
interface GeminiPlaylistService {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generatePlaylistSuggestion(
        @Query("key") apiKey: String,
        @Body body: GeminiGenerateRequest,
    ): GeminiGenerateResponse
}
