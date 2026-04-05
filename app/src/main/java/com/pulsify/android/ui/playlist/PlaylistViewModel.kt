package com.pulsify.android.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsify.android.data.repository.PulsifyRepository
import com.pulsify.android.domain.PlaybackUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class PlaylistViewModel(
    private val repository: PulsifyRepository,
) : ViewModel() {

    val playback = repository.playback.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PlaybackUiState(isPlaying = false, currentIndex = 0, tracks = emptyList()),
    )

    fun togglePlayPause() = repository.togglePlayPause()

    fun skipNext() = repository.skipNext()

    fun skipPrevious() = repository.skipPrevious()
}
