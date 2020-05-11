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
            .addMigrations(MIGRATION_1_TO_3, MIGRATION_2_TO_3)
            .build()
    }

    private val MIGRATION_1_TO_3 = (object : Migration(1, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            migrateVer2(database)
            migrateVer3(database)
        }
    })

    private val MIGRATION_2_TO_3 = (object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            migrateVer3(database)
        }
    })

    private fun migrateVer2(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE google_fit_step_counter (id INTEGER NOT NULL, stepNum INTEGER NOT NULL, dayInstant INTEGER NOT NULL, PRIMARY KEY(id))")
    }

    private fun migrateVer3(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE google_fit_step_counter ADD COLUMN distance INTEGER NOT NULL DEFAULT 01")
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideDeviceStepCounterDao(db: AppDatabase): StepCounterDao = db.deviceStepCounterDao()

    @JvmStatic
    @Provides
    @Singleton
    fun provideGoogleFitStepCounterDao(db: AppDatabase): GoogleFitDao = db.googleFitDao()
}