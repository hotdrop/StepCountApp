package jp.hotdrop.stepcountapp.di.module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import jp.hotdrop.stepcountapp.repository.local.room.AppDatabase
import jp.hotdrop.stepcountapp.repository.local.room.StepCounterDao
import javax.inject.Singleton

@Module
object DatabaseModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideDb(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "stepcountapp.db")
            .fallbackToDestructiveMigrationFrom(1)
            .build()
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideDeviceStepCounterDao(db: AppDatabase): StepCounterDao = db.deviceStepCounterDao()
}