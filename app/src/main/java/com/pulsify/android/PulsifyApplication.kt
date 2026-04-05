package com.pulsify.android

import android.app.Application
import com.pulsify.android.di.AppContainer

class PulsifyApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
