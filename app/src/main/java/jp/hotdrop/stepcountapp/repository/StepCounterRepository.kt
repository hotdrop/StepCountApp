package jp.hotdrop.stepcountapp.repository

import dagger.Reusable
import jp.hotdrop.stepcountapp.common.*
import jp.hotdrop.stepcountapp.model.DeviceDetail
import jp.hotdrop.stepcountapp.model.DailyStepCount
import jp.hotdrop.stepcountapp.repository.local.SharedPrefs
import jp.hotdrop.stepcountapp.repository.local.room.StepCounterDatabase
import jp.hotdrop.stepcountapp.repository.local.room.DailyStepCountEntity
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import javax.inject.Inject

@Reusable
class StepCounterRepository @Inject constructor(
    private val db: StepCounterDatabase,
    private val sharedPrefs: SharedPrefs
) {
    /**
     * DBにない日のデータも歩数0で取得する
     */
    suspend fun find(targetAt: ZonedDateTime): DailyStepCount {
        val ymdKey = targetAt.toLongYearMonthDay()
        return db.select(ymdKey)?.let {
            DailyStepCount(
                ymdId = it.id,
                stepNum = it.stepNum,
                dayAt = it.dayInstant.toZonedDateTime()
            )
        } ?: DailyStepCount(ymdId = ymdKey, stepNum = 0, dayAt = targetAt)
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
                    dayAt = it.dayInstant.toZonedDateTime()
                )
            }
    }

    suspend fun save(stepCount: Long) {
        // 保存は当日しか絶対しないのでcreateのなかでキーも作って保存する
        val entity = DailyStepCountEntity.create(stepNum = stepCount)
        db.save(entity)
    }

    suspend fun totalStepCountNumPreviousDate(): Long {
        val todayKey = ZonedDateTime.now().toLongYearMonthDay()
        return db.selectAll()
            .filter { it.id != todayKey }
            .sumByLong { it.stepNum }
    }

    suspend fun rangeStepCountNumPreviousDate(startAt: ZonedDateTime): Long {
        val todayKey = ZonedDateTime.now().toLongYearMonthDay()
        val startAtDateTime = startAt.toStartDateTime()
        return db.selectAll(startAtDateTime.toInstant())
            .filter { it.id != todayKey }
            .sumByLong { it.stepNum }
    }

    fun getDeviceDetail(counterFromOS: Long): DeviceDetail {
        return DeviceDetail(
            appStartFirstCounter = sharedPrefs.appStartDeviceCounter,
            initAfterRebootDateTimeEpoch = sharedPrefs.initStepCounterAfterRebootDateTime,
            stepCounterFromOS = counterFromOS
        )
    }
}