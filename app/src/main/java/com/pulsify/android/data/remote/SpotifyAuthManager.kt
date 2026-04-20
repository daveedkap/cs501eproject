package com.pulsify.android.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Base64
import com.pulsify.android.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom

class SpotifyAuthManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("spotify_auth", Context.MODE_PRIVATE)
    private val httpClient = OkHttpClient()

    private var codeVerifier: String? = null

    private val _isAuthenticated = MutableStateFlow(
        prefs.getString(KEY_ACCESS_TOKEN, null) != null,
    )
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _displayName = MutableStateFlow(prefs.getString(KEY_DISPLAY_NAME, null))
    val displayName: StateFlow<String?> = _displayName.asStateFlow()

    fun buildAuthUrl(): String {
        val verifier = generateCodeVerifier()
        codeVerifier = verifier
        val challenge = generateCodeChallenge(verifier)

        return Uri.Builder()
            .scheme("https")
            .authority("accounts.spotify.com")
            .appendPath("authorize")
            .appendQueryParameter("client_id", BuildConfig.SPOTIFY_CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", BuildConfig.SPOTIFY_REDIRECT_URI)
            .appendQueryParameter("scope", SCOPES)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("code_challenge", challenge)
            .build()
            .toString()
    }

    suspend fun handleCallback(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val code = uri.getQueryParameter("code") ?: return@withContext false
        val verifier = codeVerifier ?: return@withContext false

        val body = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", BuildConfig.SPOTIFY_REDIRECT_URI)
            .add("client_id", BuildConfig.SPOTIFY_CLIENT_ID)
            .add("code_verifier", verifier)
            .build()

        val request = Request.Builder()
            .url("https://accounts.spotify.com/api/token")
            .post(body)
            .build()

        val response = runCatching { httpClient.newCall(request).execute() }.getOrNull()
            ?: return@withContext false

        if (!response.isSuccessful) {
            response.close()
            return@withContext false
        }

        val responseBody = response.body?.string() ?: return@withContext false
        val json = runCatching { JSONObject(responseBody) }.getOrNull()
            ?: return@withContext false

        val accessToken = json.optString("access_token", "").ifEmpty { return@withContext false }
        val refreshToken = json.optString("refresh_token", "").ifEmpty { null }
        val expiresIn = json.optInt("expires_in", 3600)

        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_EXPIRES_AT, System.currentTimeMillis() + expiresIn * 1000L)
            .apply()

        codeVerifier = null
        _isAuthenticated.value = true
        true
    }

    suspend fun getValidAccessToken(): String? = withContext(Dispatchers.IO) {
        val token = prefs.getString(KEY_ACCESS_TOKEN, null) ?: return@withContext null
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)

        if (System.currentTimeMillis() < expiresAt - 60_000) return@withContext token

        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
            ?: return@withContext null

        val body = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", refreshToken)
            .add("client_id", BuildConfig.SPOTIFY_CLIENT_ID)
            .build()

        val request = Request.Builder()
            .url("https://accounts.spotify.com/api/token")
            .post(body)
            .build()

        val response = runCatching { httpClient.newCall(request).execute() }.getOrNull()
            ?: return@withContext null

        if (!response.isSuccessful) {
            response.close()
            logout()
            return@withContext null
        }

        val responseBody = response.body?.string() ?: return@withContext null
        val json = runCatching { JSONObject(responseBody) }.getOrNull()
            ?: return@withContext null

        val newAccessToken = json.optString("access_token", "").ifEmpty { return@withContext null }
        val newRefreshToken = json.optString("refresh_token", refreshToken ?: "")
        val newExpiresIn = json.optInt("expires_in", 3600)

        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, newAccessToken)
            .putString(KEY_REFRESH_TOKEN, newRefreshToken)
            .putLong(KEY_EXPIRES_AT, System.currentTimeMillis() + newExpiresIn * 1000L)
            .apply()

        _isAuthenticated.value = true
        newAccessToken
    }

    fun logout() {
        prefs.edit().clear().apply()
        _isAuthenticated.value = false
        _displayName.value = null
    }

    fun updateDisplayName(name: String?) {
        _displayName.value = name
        prefs.edit().putString(KEY_DISPLAY_NAME, name).apply()
    }

    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(64)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun generateCodeChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val SCOPES = "user-read-recently-played user-top-read"
    }
}
