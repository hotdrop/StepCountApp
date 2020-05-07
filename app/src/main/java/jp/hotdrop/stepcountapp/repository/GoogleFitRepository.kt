package jp.hotdrop.stepcountapp.repository

import dagger.Reusable
import jp.hotdrop.stepcountapp.common.toEndDateTime
import jp.hotdrop.stepcountapp.common.toLongYearMonthDay
import jp.hotdrop.stepcountapp.common.toStartDateTime
import jp.hotdrop.stepcountapp.common.toZonedDateTime
import jp.hotdrop.stepcountapp.model.DailyStepCount
import jp.hotdrop.stepcountapp.repository.local.room.GoogleFitDatabase
import jp.hotdrop.stepcountapp.repository.local.room.GoogleFitEntity
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import javax.inject.Inject

@Reusable
class GoogleFitRepository @Inject constructor(
    private val db: GoogleFitDatabase
) {
    suspend fun find(targetAt: ZonedDateTime): DailyStepCount? {
        val key = targetAt.toLongYearMonthDay()
        return db.select(key)?.let {
            DailyStepCount(
                ymdId = it.id,
                stepNum = it.stepNum,
                dayAt = it.dayInstant.toZonedDateTime()
            )
        }
    }

    suspend fun findRange(startAt: ZonedDateTime, endAt: ZonedDateTime): List<DailyStepCount> {
        val startAtDateTime = startAt.toStartDateTime()
        val endAtDateTime = endAt.toEndDateTime()
        Timber.d("$startAtDateTime から $endAtDateTime の範囲を取得")

        return db.selectAll(startAtDateTime.toInstant(), endAtDateTime.toInstant())
            .map {
                DailyStepCount(it.id, it.stepNum, it.dayInstant.toZonedDateTime())
            }
    }

    suspend fun save(stepNum: Long, dayAt: ZonedDateTime) {
        val key = dayAt.toLongYearMonthDay()
        val entity = GoogleFitEntity(key, stepNum, dayAt.toInstant())
        db.save(entity)
    }
}