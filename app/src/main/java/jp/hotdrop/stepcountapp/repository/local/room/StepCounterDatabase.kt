package jp.hotdrop.stepcountapp.repository.local.room

import androidx.room.*
import dagger.Reusable
import jp.hotdrop.stepcountapp.common.toLongYearMonthDay
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

@Reusable
class StepCounterDatabase @Inject constructor(
    private val dao: StepCounterDao
) {
    suspend fun select(id: Long): DailyStepCountEntity? {
        return dao.select(id)
    }

    suspend fun selectAll(): List<DailyStepCountEntity> {
        return dao.selectAll()
    }

    suspend fun selectAll(startAt: Instant): List<DailyStepCountEntity> {
        return dao.selectAll(startAt)
    }

    suspend fun selectAll(startAt: Instant, endAt: Instant): List<DailyStepCountEntity> {
        return dao.selectAll(startAt, endAt)
    }

    suspend fun save(entity: DailyStepCountEntity) {
        dao.insert(entity)
    }
}

@Dao
interface StepCounterDao {

    @Query("SELECT * FROM ${DailyStepCountEntity.TABLE_NAME} WHERE id == :id")
    suspend fun select(id: Long): DailyStepCountEntity?

    @Query("SELECT * FROM ${DailyStepCountEntity.TABLE_NAME}")
    suspend fun selectAll(): List<DailyStepCountEntity>

    @Query("SELECT * FROM ${DailyStepCountEntity.TABLE_NAME} WHERE dayInstant > :startAt")
    suspend fun selectAll(startAt: Instant): List<DailyStepCountEntity>

    @Query("SELECT * FROM ${DailyStepCountEntity.TABLE_NAME} WHERE dayInstant BETWEEN :startAt AND :endAt")
    suspend fun selectAll(startAt: Instant, endAt: Instant): List<DailyStepCountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DailyStepCountEntity)
}

@Entity(tableName = DailyStepCountEntity.TABLE_NAME)
data class DailyStepCountEntity(
    @PrimaryKey val id: Long,
    val stepNum: Long,
    val dayInstant: Instant
) {
    companion object {
        const val TABLE_NAME = "step_counter"
        fun create(id: Long? = null, stepNum: Long): DailyStepCountEntity {
            val now = ZonedDateTime.now()
            val key = id ?: now.toLongYearMonthDay()
            return DailyStepCountEntity(key, stepNum, now.toInstant())
        }
    }
}