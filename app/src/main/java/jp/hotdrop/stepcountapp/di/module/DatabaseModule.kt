package jp.hotdrop.stepcountapp.di.module

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import jp.hotdrop.stepcountapp.repository.local.room.AppDatabase
import jp.hotdrop.stepcountapp.repository.local.room.GoogleFitDao
import jp.hotdrop.stepcountapp.repository.local.room.StepCounterDao
import javax.inject.Singleton

@Module
object DatabaseModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideDb(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "stepcountapp.db")
            .addMigrations(MIGRATION_1_TO_2)
            .build()
    }

    private val MIGRATION_1_TO_2 = (object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE google_fit_step_counter (id INTEGER NOT NULL, stepNum INTEGER NOT NULL, dayInstant INTEGER NOT NULL, PRIMARY KEY(id))")
        }
    })

    @JvmStatic
    @Provides
    @Singleton
    fun provideDeviceStepCounterDao(db: AppDatabase): StepCounterDao = db.deviceStepCounterDao()

    @JvmStatic
    @Provides
    @Singleton
    fun provideGoogleFitStepCounterDao(db: AppDatabase): GoogleFitDao = db.googleFitDao()
}