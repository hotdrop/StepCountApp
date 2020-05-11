package jp.hotdrop.stepcountapp.repository.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        (DailyStepCountEntity::class),
        (GoogleFitEntity::class)
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceStepCounterDao(): StepCounterDao
    abstract fun googleFitDao(): GoogleFitDao
}