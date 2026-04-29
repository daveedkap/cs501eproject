package com.pulsify.android.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsify.android.data.repository.PulsifyRepository
import com.pulsify.android.domain.PlaybackUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val repository: PulsifyRepository,
) : ViewModel() {

    val playback = repository.playback.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PlaybackUiState(isPlaying = false, currentIndex = 0, tracks = emptyList()),
    )

    fun togglePlayPause() {
        viewModelScope.launch { repository.togglePlayPause() }
    }

    fun skipNext() {
        viewModelScope.launch { repository.skipNext() }
    }

    fun skipPrevious() {
        viewModelScope.launch { repository.skipPrevious() }
    }

    fun removeTrack(trackId: String) {
        viewModelScope.launch { repository.removeTrack(trackId) }
    }
}
