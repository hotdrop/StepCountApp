package jp.hotdrop.stepcountapp.di.component

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import jp.hotdrop.stepcountapp.di.module.DatabaseModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    DatabaseModule::class
])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun applicationContext(appContext: Context): Builder
        fun build(): AppComponent
    }

    fun plus(): ActivityComponent
}