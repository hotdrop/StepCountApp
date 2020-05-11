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
                distance = it.distance,
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
                DailyStepCount(
                    ymdId = it.id,
                    stepNum = it.stepNum,
                    distance = it.distance,
                    dayAt = it.dayInstant.toZonedDateTime()
                )
            }
    }

    suspend fun save(newStepNum: Long, newDistance: Long, dayAt: ZonedDateTime) {
        val key = dayAt.toLongYearMonthDay()
        val entity = GoogleFitEntity(key, newStepNum, newDistance, dayAt.toInstant())
        db.save(entity)
    }

    suspend fun saveStepNum(newStepNum: Long, dayAt: ZonedDateTime) {
        val key = dayAt.toLongYearMonthDay()
        val alreadyEntity = db.select(key) ?: GoogleFitEntity(key, 0, 0, dayAt.toInstant())
        val entity = alreadyEntity.copy(stepNum = newStepNum)
        db.save(entity)
    }

    suspend fun saveDistance(newDistance: Long, dayAt: ZonedDateTime) {
        val key = dayAt.toLongYearMonthDay()
        val alreadyEntity = db.select(key) ?: GoogleFitEntity(key, 0, 0, dayAt.toInstant())
        val entity = alreadyEntity.copy(distance = newDistance)
        db.save(entity)
    }
}