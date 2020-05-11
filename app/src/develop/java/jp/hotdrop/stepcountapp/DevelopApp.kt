package jp.hotdrop.stepcountapp

import com.facebook.stetho.Stetho
import timber.log.Timber

class DevelopApp : App() {

    override fun onCreate() {
        super.onCreate()

        initTimber()
        initStetho()
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }

    private fun initStetho() {
        Stetho.initializeWithDefaults(this)
    }
}