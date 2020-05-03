package jp.hotdrop.stepcountapp.repository

import dagger.Reusable
import jp.hotdrop.stepcountapp.common.sumByLong
import jp.hotdrop.stepcountapp.common.toLongYearMonthDay
import jp.hotdrop.stepcountapp.common.toZonedDateTime
import jp.hotdrop.stepcountapp.model.DeviceDetail
import jp.hotdrop.stepcountapp.model.DailyStepCount
import jp.hotdrop.stepcountapp.repository.local.SharedPrefs
import jp.hotdrop.stepcountapp.repository.local.room.StepCounterDatabase
import jp.hotdrop.stepcountapp.repository.local.room.DailyStepCountEntity
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

@Reusable
class StepCounterRepository @Inject constructor(
    private val db: StepCounterDatabase,
    private val sharedPrefs: SharedPrefs
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

    suspend fun totalCountPreviousDateStepNum(): Long {
        val todayKey = ZonedDateTime.now().toLongYearMonthDay()
        return db.selectAll()
            .filter { it.id != todayKey }
            .sumByLong { it.stepNum }
    }

    suspend fun save(counter: Long) {
        // 保存は当日しか絶対しないのでcreateのなかでキーも作って保存する
        val entity = DailyStepCountEntity.create(stepNum = counter)
        db.save(entity)
    }

    fun getDeviceDetail(counterFromOS: Long): DeviceDetail {
        return DeviceDetail(
            appStartFirstCounter = sharedPrefs.appStartDeviceCounter,
            initAfterRebootDateTimeEpoch = sharedPrefs.initStepCounterAfterRebootDateTime,
            stepCounterFromOS = counterFromOS
        )
    }
}