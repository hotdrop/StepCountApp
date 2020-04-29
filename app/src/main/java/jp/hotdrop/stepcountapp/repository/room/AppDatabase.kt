package jp.hotdrop.stepcountapp.repository.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [(DeviceStepCounterEntity::class)],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceStepCounterDao(): DeviceStepCounterDao
}