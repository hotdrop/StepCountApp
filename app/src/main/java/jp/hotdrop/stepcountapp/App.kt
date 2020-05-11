package jp.hotdrop.stepcountapp

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.jakewharton.threetenabp.AndroidThreeTen
import jp.hotdrop.stepcountapp.di.component.AppComponent
import jp.hotdrop.stepcountapp.di.component.DaggerAppComponent
import jp.hotdrop.stepcountapp.di.component.DaggerComponentProvider

open class App : Application(), DaggerComponentProvider {

    override lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()

        initThreeTen()

        component = DaggerAppComponent.builder()
            .applicationContext(this)
            .build()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun initThreeTen() {
        AndroidThreeTen.init(this)
    }
}