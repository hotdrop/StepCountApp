package jp.hotdrop.stepcountapp.repository.local.room

import androidx.lifecycle.LiveData
import androidx.room.*
import dagger.Reusable
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

@Reusable
class StepCounterDatabase @Inject constructor(
    private val dao: StepCounterDao
) {
    fun select(id: Long): LiveData<DailyStepCountEntity?> {
        return dao.select(id)
    }

    suspend fun selectAll(): List<DailyStepCountEntity> {
        return dao.selectAll()
    }

    suspend fun save(entity: DailyStepCountEntity) {
        dao.insert(entity)
    }
}

@Dao
interface StepCounterDao {

    @Query("SELECT * FROM ${DailyStepCountEntity.TABLE_NAME} WHERE id == :id")
    fun select(id: Long): LiveData<DailyStepCountEntity?>

    @Query("SELECT * FROM ${DailyStepCountEntity.TABLE_NAME}")
    suspend fun selectAll(): List<DailyStepCountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DailyStepCountEntity)
}

@Entity(tableName = DailyStepCountEntity.TABLE_NAME)
data class DailyStepCountEntity(
    @PrimaryKey val id: Long,
    val stepNum: Long,
    val dayAt: Instant
) {
    companion object {
        const val TABLE_NAME = "step_counter"
        fun create(id: Long? = null, stepNum: Long): DailyStepCountEntity {
            val now = ZonedDateTime.now()
            val key = id ?: makeKey(now)
            return DailyStepCountEntity(key, stepNum, now.toInstant())
        }

        fun makeKey(now: ZonedDateTime): Long {
            val monthStr = now.month.value.toString().padStart(2, '0')
            val dayStr = now.dayOfMonth.toString().padStart(2, '0')
            return "${now.year}${monthStr}${dayStr}".toLong()
        }
    }
}