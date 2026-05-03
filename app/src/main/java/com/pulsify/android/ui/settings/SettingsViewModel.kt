package com.pulsify.android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsify.android.data.remote.SpotifyAuthManager
import com.pulsify.android.data.repository.PulsifyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    private val repository: PulsifyRepository,
    val spotifyAuthManager: SpotifyAuthManager,
) : ViewModel() {

    val spotifyLinked = repository.spotifyLinked.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        false,
    )

    val spotifyDisplayName = spotifyAuthManager.displayName.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )

    val textModePreferred = repository.textModePreferred.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        false,
    )

    fun buildSpotifyAuthUrl(): String = spotifyAuthManager.buildAuthUrl()

    fun disconnectSpotify() {
        spotifyAuthManager.logout()
        repository.clearSpotifySessionCatalog()
    }

    fun setTextMode(value: Boolean) = repository.setTextModePreferred(value)

    fun resetChat() = repository.clearConversation()
}
