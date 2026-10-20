package io.github.datend3nker.openwrist

import android.app.Application

/** Creates the [AppContainer] once per process and schedules the update check. */
class OpenWristApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        UpdateCheckWorker.schedule(this)
    }
}
