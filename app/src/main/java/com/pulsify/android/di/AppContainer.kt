package com.pulsify.android.di

import android.content.Context
import com.pulsify.android.data.local.PulsifyDatabase
import com.pulsify.android.data.location.LocationReader
import com.pulsify.android.data.remote.NetworkModule
import com.pulsify.android.data.repository.PulsifyRepository
import com.pulsify.android.data.sensor.ActivityClassifier

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val database: PulsifyDatabase = PulsifyDatabase.build(appContext)

    val geminiService = NetworkModule.geminiService()

    val spotifyWebService = NetworkModule.spotifyService()

    val repository = PulsifyRepository(
        dao = database.pulsifyDao(),
        geminiPlaylistService = geminiService,
    )

    val locationReader = LocationReader(appContext)

    fun newActivityClassifier() = ActivityClassifier()
}
