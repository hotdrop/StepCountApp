package jp.hotdrop.stepcountapp.repository

import dagger.Reusable
import jp.hotdrop.stepcountapp.common.toLongYearMonthDay
import jp.hotdrop.stepcountapp.common.toZonedDateTime
import jp.hotdrop.stepcountapp.model.DailyStepCount
import jp.hotdrop.stepcountapp.repository.local.room.GoogleFitDatabase
import jp.hotdrop.stepcountapp.repository.local.room.GoogleFitEntity
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

@Reusable
class GoogleFitRepository @Inject constructor(
    private val db: GoogleFitDatabase
) {
    suspend fun find(targetAt: ZonedDateTime): DailyStepCount? {
        val key = targetAt.toLongYearMonthDay()
        return db.select(key)?.let {
            DailyStepCount(
                stepNum = it.stepNum,
                dayAt = it.dayInstant.toZonedDateTime()
            )
        }
    }

    suspend fun save(dailyStepCount: DailyStepCount) {
        val key = dailyStepCount.dayAt.toLongYearMonthDay()
        val entity = GoogleFitEntity(key, dailyStepCount.stepNum, dailyStepCount.dayAt.toInstant())
        db.save(entity)
    }
}