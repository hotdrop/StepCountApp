package jp.hotdrop.stepcountapp.repository.local.room

import androidx.room.*
import dagger.Reusable
import jp.hotdrop.stepcountapp.common.toLongYearMonthDay
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

@Reusable
class GoogleFitDatabase @Inject constructor(
    private val dao: GoogleFitDao
) {
    suspend fun select(id: Long): GoogleFitEntity? {
        return dao.select(id)
    }

    suspend fun selectAll(startAt: Instant, endAt: Instant): List<GoogleFitEntity> {
        return dao.selectAll(startAt, endAt)
    }

    suspend fun save(entity: GoogleFitEntity) {
        dao.insert(entity)
    }
}

@Dao
interface GoogleFitDao {

    @Query("SELECT * FROM ${GoogleFitEntity.TABLE_NAME} WHERE id == :id")
    suspend fun select(id: Long): GoogleFitEntity?

    @Query("SELECT * FROM ${GoogleFitEntity.TABLE_NAME} WHERE dayInstant BETWEEN :startAt AND :endAt")
    suspend fun selectAll(startAt: Instant, endAt: Instant): List<GoogleFitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GoogleFitEntity)
}

@Entity(tableName = GoogleFitEntity.TABLE_NAME)
data class GoogleFitEntity(
    @PrimaryKey val id: Long,
    val stepNum: Long,
    val dayInstant: Instant
) {
    companion object {
        const val TABLE_NAME = "google_fit_step_counter"
        fun create(id: Long? = null, stepNum: Long): GoogleFitEntity {
            val now = ZonedDateTime.now()
            val key = id ?: now.toLongYearMonthDay()
            return GoogleFitEntity(key, stepNum, now.toInstant())
        }
    }
}