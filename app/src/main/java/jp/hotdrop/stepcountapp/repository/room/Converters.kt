package jp.hotdrop.stepcountapp.repository.room

import androidx.room.TypeConverter
import org.threeten.bp.Instant

object Converters {

    @TypeConverter
    @JvmStatic
    fun fromTimestamp(value: Long?): Instant? = value?.let { Instant.ofEpochSecond(value) }

    @TypeConverter
    @JvmStatic
    fun toTimestamp(instant: Instant?): Long? = instant?.epochSecond
}