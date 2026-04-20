package com.pulsify.android.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiPlaylistService {
    @POST("v1beta/models/gemini-2.0-flash-lite:generateContent")
    suspend fun generatePlaylistSuggestion(
        @Query("key") apiKey: String,
        @Body body: GeminiGenerateRequest,
    ): GeminiGenerateResponse
}
