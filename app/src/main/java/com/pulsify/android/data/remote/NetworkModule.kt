package com.pulsify.android.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun client(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    fun geminiRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(client())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    fun spotifyRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.spotify.com/")
        .client(client())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    fun geminiService(): GeminiPlaylistService = geminiRetrofit().create(GeminiPlaylistService::class.java)

    fun spotifyService(): SpotifyWebService = spotifyRetrofit().create(SpotifyWebService::class.java)
}
