package com.pulsify.android.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pulsify.android.PulsifyApplication
import com.pulsify.android.ui.home.HomeViewModel
import com.pulsify.android.ui.map.MapViewModel
import com.pulsify.android.ui.playlist.PlaylistViewModel
import com.pulsify.android.ui.sessions.SessionsViewModel
import com.pulsify.android.ui.settings.SettingsViewModel

class PulsifyViewModelFactory(
    private val application: Application,
    private val container: AppContainer,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val c = container
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(
                application,
                c.repository,
                c.newActivityClassifier(),
                c.locationReader,
            ) as T

            modelClass.isAssignableFrom(SessionsViewModel::class.java) -> SessionsViewModel(
                c.repository,
            ) as T

            modelClass.isAssignableFrom(MapViewModel::class.java) -> MapViewModel(
                c.locationReader,
                c.repository,
            ) as T

            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(
                c.repository,
            ) as T

            modelClass.isAssignableFrom(PlaylistViewModel::class.java) -> PlaylistViewModel(
                c.repository,
            ) as T

            else -> throw IllegalArgumentException("Unknown ViewModel ${modelClass.name}")
        }
    }

    companion object {
        fun from(app: PulsifyApplication): PulsifyViewModelFactory =
            PulsifyViewModelFactory(app, app.container)
    }
}
