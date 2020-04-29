package jp.hotdrop.stepcountapp

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.jakewharton.threetenabp.AndroidThreeTen
import jp.hotdrop.stepcountapp.di.component.AppComponent
import jp.hotdrop.stepcountapp.di.component.DaggerAppComponent
import jp.hotdrop.stepcountapp.di.component.DaggerComponentProvider
import timber.log.Timber

class App : Application(), DaggerComponentProvider {

    override lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()

        initTimber()
        initThreeTen()

        component = DaggerAppComponent.builder()
            .applicationContext(this)
            .build()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // TODO リリースはクラッシュ飛ばす
    }

    private fun initThreeTen() {
        AndroidThreeTen.init(this)
    }
}