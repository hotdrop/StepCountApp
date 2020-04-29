package jp.hotdrop.stepcountapp.repository.room

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import dagger.Reusable
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

@Reusable
class DeviceStepCounterDatabase @Inject constructor(
    private val dao: DeviceStepCounterDao
) {
    suspend fun select(startAt: ZonedDateTime, endAt: ZonedDateTime): List<DeviceStepCounterEntity> {
        return dao.select(startAt.toEpochSecond(), endAt.toEpochSecond())
    }
}

@Dao
interface DeviceStepCounterDao {
    @Query("SELECT * FROM ${DeviceStepCounterEntity.TABLE_NAME} WHERE dayAt BETWEEN :startEpoch AND :endEpoch")
    suspend fun select(startEpoch: Long, endEpoch: Long): List<DeviceStepCounterEntity>
}

@Entity(tableName = DeviceStepCounterEntity.TABLE_NAME)
data class DeviceStepCounterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    val stepNum: Long,
    val dayAt: Instant
) {
    companion object {
        const val TABLE_NAME = "device_step_counter"
    }
}